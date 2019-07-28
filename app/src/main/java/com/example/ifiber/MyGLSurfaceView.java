package com.example.ifiber;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class MyGLSurfaceView extends GLSurfaceView {

    public final MyGLRenderer mRenderer;
    private SparseArray<PointF> mActivePointers;

    public MyGLSurfaceView(Context context, MyGLRenderer renderer) {
        super(context);
        setEGLContextClientVersion(3);
        mRenderer=renderer;
        mRenderer.setContext(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); // Render the view only when there is a change in the drawing data
        mActivePointers = new SparseArray<>();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // get pointer index from the event object
        int pointerIndex = e.getActionIndex();

        // get pointer ID
        int pointerId = e.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = e.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                PointF f = new PointF();
                f.x = e.getX(pointerIndex);
                f.y = e.getY(pointerIndex);
                mActivePointers.put(pointerId, f);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int size = e.getPointerCount();

                if (size == 1) {
                    float dx = e.getX(0) - mActivePointers.get(e.getPointerId(0)).x;
                    float dy = e.getY(0) - mActivePointers.get(e.getPointerId(0)).y;

                    mRenderer.orbitCam(dx, dy);
                    requestRender();
                }
                else if (size == 2) {
                    float delta = (float)((Math.sqrt(Math.pow(mActivePointers.get(e.getPointerId(0)).x - mActivePointers.get(e.getPointerId(1)).x, 2) + Math.pow(mActivePointers.get(e.getPointerId(0)).y - mActivePointers.get(e.getPointerId(1)).y, 2))) -
                            (Math.sqrt(Math.pow(e.getX(0) - e.getX(1), 2) + Math.pow(e.getY(0) - e.getY(1), 2))));

                    mRenderer.zoomCam(delta);
                    requestRender();
                }
                else {
                    PointF prevAv = new PointF(0.0f, 0.0f);
                    PointF actuAv = new PointF(0.0f, 0.0f);
                    for(int i=0; i<size; i++) {
                        PointF point = mActivePointers.get(e.getPointerId(i));
                        prevAv.x += point.x;
                        prevAv.y += point.y;

                        actuAv.x += e.getX(i);
                        actuAv.y += e.getY(i);
                    }

                    prevAv.x /= size;
                    prevAv.y /= size;

                    actuAv.x /= size;
                    actuAv.y /= size;

                    mRenderer.panCam(actuAv.x-prevAv.x, actuAv.y-prevAv.y);
                    requestRender();
                }

                for(int i=0; i<size; i++) {
                    PointF point = mActivePointers.get(e.getPointerId(i));
                    if (point != null) {
                        point.x = e.getX(i);
                        point.y = e.getY(i);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointers.remove(pointerId);
                break;
            }
        }
        invalidate();
        return true;
    }
}


