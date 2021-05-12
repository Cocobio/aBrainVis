// https://github.com/rii-mango/GIFTI-IO
package com.udec_biomed.aBrainVis.Tools.ThirdParty.Gifti;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class DataArray implements MetadataHolder {

    private final Map<String, String> attributes;
    private Buffer buffer;
    private final Map<String, String> metadata;
    private final Vector<GiftiTransform> transforms;

    public static final String ATT_ARRAYINDEXINGORDER = "ArrayIndexingOrder";
    public static final String ATT_DATATYPE = "DataType";
    public static final String ATT_DIMENSIONALITY = "Dimensionality";
    public static final String ATT_DIMN = "Dim";
    public static final String ATT_ENCODING = "Encoding";
    public static final String ATT_ENDIAN = "Endian";
    public static final String ATT_EXTERNALFILENAME = "ExternalFileName";
    public static final String ATT_EXTERNALFILEOFFSET = "ExternalFileOffset";
    public static final String ATT_INTENT = "Intent";

    public static final String DATA_ORDER_BIGENDIAN = "BigEndian";
    public static final String DATA_ORDER_LITTLEENDIAN = "LittleEndian";

    public static final String DIM_ORDER_COLUMNMAJORORDER = "ColumnMajorOrder";
    public static final String DIM_ORDER_ROWMAJORORDER = "RowMajorOrder";
    public static final String ENCODING_ASCII = "ASCII";

    public static final String ENCODING_BASE64BINARY = "Base64Binary";
    public static final String ENCODING_EXTERNALFILEBINARY = "ExternalFileBinary";
    public static final String ENCODING_GZIPBASE64BINARY = "GZipBase64Binary";
    public static final String NIFTI_INTENT_GENMATRIX = "NIFTI_INTENT_GENMATRIX";

    public static final String NIFTI_INTENT_LABEL = "NIFTI_INTENT_LABEL";
    public static final String NIFTI_INTENT_NODE_INDEX = "NIFTI_INTENT_NODE_INDEX";

    public static final String NIFTI_INTENT_NONE = "NIFTI_INTENT_NONE";
    public static final String NIFTI_INTENT_POINTSET = "NIFTI_INTENT_POINTSET";
    public static final String NIFTI_INTENT_RGB_VECTOR = "NIFTI_INTENT_RGB_VECTOR";
    public static final String NIFTI_INTENT_RGBA_VECTOR = "NIFTI_INTENT_RGBA_VECTOR";
    public static final String NIFTI_INTENT_SHAPE = "NIFTI_INTENT_SHAPE";
    public static final String NIFTI_INTENT_TIME_SERIES = "NIFTI_INTENT_TIME_SERIES";
    public static final String NIFTI_INTENT_TRIANGLE = "NIFTI_INTENT_TRIANGLE";
    public static final String NIFTI_INTENT_VECTOR = "NIFTI_INTENT_VECTOR";
    public static final String TYPE_NIFTI_TYPE_FLOAT32 = "NIFTI_TYPE_FLOAT32";
    public static final String TYPE_NIFTI_TYPE_INT32 = "NIFTI_TYPE_INT32";
    public static final String TYPE_NIFTI_TYPE_UINT8 = "NIFTI_TYPE_UINT8";



    /**
     * @param attributes
     */
    public DataArray(final Map<String, String> attributes, final boolean headerOnly) {
        this.attributes = attributes;
        metadata = new HashMap<String, String>();
        transforms = new Vector<GiftiTransform>();

        if (!headerOnly) {
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(getDimensionality() * getBytesPerUnit());
            byteBuffer.order(ByteOrder.nativeOrder());
            buffer = byteBuffer;
        }
    }



    /**
     * @param attributes
     * @param buffer
     */
    public DataArray(final Map<String, String> attributes, final Buffer buffer) {
        this.attributes = attributes;
        metadata = new HashMap<String, String>();
        transforms = new Vector<GiftiTransform>();
        this.buffer = buffer;
    }



    /* (non-Javadoc)
     * @see edu.uthscsa.ric.visualization.surface.io.formats.gifti.MetadataHolder#addMetadata(java.util.Map)
     */
    @Override
    public void addMetadata(final Map<String, String> metadata) {
        this.metadata.putAll(metadata);
    }



    /**
     * @param xform
     */
    public void addTransform(final GiftiTransform xform) {
        transforms.add(xform);
    }



    /**
     *
     * @return
     */
    public FloatBuffer getAsFloatBuffer() {
        if (buffer != null) {
            buffer.rewind();

            if (buffer instanceof FloatBuffer) {
                return (FloatBuffer) buffer;
            } else if (buffer instanceof ByteBuffer) {
                return ((ByteBuffer) buffer).asFloatBuffer();
            }
        }

        return null;
    }



    /**
     *
     * @return
     */
    public IntBuffer getAsIntBuffer() {
        if (buffer != null) {
            buffer.rewind();
            if (buffer instanceof IntBuffer) {
                return (IntBuffer) buffer;
            } else if (buffer instanceof ByteBuffer) {
                return ((ByteBuffer) buffer).asIntBuffer();
            }
        }

        return null;
    }



    /**
     * @return
     */
    public ByteBuffer getAsByteBuffer() {
        if (buffer != null) {
            buffer.rewind();

            if (buffer instanceof ByteBuffer) {
                return (ByteBuffer) buffer;
            }
        }

        return null;
    }



    /**
     * @return
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }



    /**
     * @return
     */
    public Buffer getBuffer() {
        return buffer;
    }



    /**
     *
     * @return
     */
    public final int getBytesPerUnit() {
        if (isFloat32()) {
            return 4;
        } else if (isInt32()) {
            return 4;
        } else if (isUnsignedInt8()) {
            return 1;
        }

        return 0;
    }



    /**
     *
     * @return
     */
    public final int getDimensionality() {
        int total = 1;
        for (int ctr = 0; ctr < getDimensions(); ctr++) {
            total *= getNumElements(ctr);
        }

        return total;
    }



    /**
     * @return
     */
    public final int getDimensions() {
        int dims = 0;
        try {
            dims = Integer.parseInt(attributes.get(ATT_DIMENSIONALITY));
        } catch (final NumberFormatException ex) {}
        return dims;
    }



    /**
     * @return
     */
    public String getExternalFilename() {
        return attributes.get(ATT_EXTERNALFILENAME);
    }



    /**
     * @return
     */
    public int getExternalFileOffset() {
        int num = 0;
        try {
            num = Integer.parseInt(attributes.get(ATT_EXTERNALFILEOFFSET));
        } catch (final NumberFormatException ex) {}
        return num;
    }



    /**
     * @return
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }



    /**
     * @return
     */
    public final int getNumElements() {
        return getNumElements(0);
    }



    /**
     * @param dim
     * @return
     */
    public int getNumElements(final int dim) {
        int num = 0;
        try {
            num = Integer.parseInt(attributes.get(ATT_DIMN + dim));
        } catch (final NumberFormatException ex) {}
        return num;
    }



    /**
     * @return
     */
    public int getNumTransforms() {
        return transforms.size();
    }



    /**
     * @return
     */
    public int getNumValues() {
        if (getDimensions() <= 2) {
            if (isScalar()) {
                return getNumElements();
            } else if (isTriple()) {
                return getNumElements() * 3;
            } else if (isQuadruple()) {
                return getNumElements() * 4;
            }
        }

        return -1;
    }



    /**
     * @return
     */
    public Vector<GiftiTransform> getTransforms() {
        return transforms;
    }



    /**
     * @return
     */
    public boolean isAscii() {
        return ENCODING_ASCII.equals(attributes.get(ATT_ENCODING));
    }



    /**
     * @return
     */
    public boolean isBase64Binary() {
        return ENCODING_BASE64BINARY.equals(attributes.get(ATT_ENCODING));
    }



    /**
     * @return
     */
    public boolean isBase64Encoded() {
        return isBase64Binary() || isGzipBase64Binary();
    }



    /**
     * @return
     */
    public boolean isExternalFileBinary() {
        return ENCODING_EXTERNALFILEBINARY.equals(attributes.get(ATT_ENCODING));
    }



    /**
     * @return
     */
    public final boolean isFloat32() {
        return TYPE_NIFTI_TYPE_FLOAT32.equals(attributes.get(ATT_DATATYPE));
    }



    /**
     * @return
     */
    public boolean isGzipBase64Binary() {
        return ENCODING_GZIPBASE64BINARY.equals(attributes.get(ATT_ENCODING));
    }



    /**
     * @return
     */
    public final boolean isInt32() {
        return TYPE_NIFTI_TYPE_INT32.equals(attributes.get(ATT_DATATYPE));
    }



    /**
     * @return
     */
    public final boolean isLabels() {
        return NIFTI_INTENT_LABEL.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isLittleEndian() {
        return DATA_ORDER_LITTLEENDIAN.equals(attributes.get(ATT_ENDIAN));
    }



    /**
     * @return
     */
    public boolean isNormals() {
        return NIFTI_INTENT_VECTOR.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isPoints() {
        return NIFTI_INTENT_POINTSET.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isQuadruple() {
        return ((getDimensions() == 2) && (getNumElements(1) == 4));
    }



    /**
     * @return
     */
    public boolean isRGB() {
        return NIFTI_INTENT_RGB_VECTOR.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isRGBA() {
        return NIFTI_INTENT_RGBA_VECTOR.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isRowMajorOrder() {
        return !DIM_ORDER_COLUMNMAJORORDER.equals(attributes.get(ATT_ARRAYINDEXINGORDER));
    }



    /**
     * @return
     */
    public boolean isScalar() {
        return (getDimensions() == 1);
    }



    /**
     * @return
     */
    public boolean isIndices() {
        return NIFTI_INTENT_TRIANGLE.equals(attributes.get(ATT_INTENT));
    }



    /**
     * @return
     */
    public boolean isTriple() {
        return ((getDimensions() == 2) && (getNumElements(1) == 3));
    }



    /**
     * @return
     */
    public final boolean isUnsignedInt8() {
        return TYPE_NIFTI_TYPE_UINT8.equals(attributes.get(ATT_DATATYPE));
    }
}