#version 320 es

precision highp float;
precision highp sampler3D;

in vec3 texCoord;

uniform sampler3D mriTexture;
uniform float slope;
uniform float bright;
uniform float contrast;

uniform float threshold;

out vec4 outColor;

void main() {
	float pixelValue = texture(mriTexture, texCoord).r;

	if (pixelValue < threshold)
		discard;

	float grey = bright + pixelValue * contrast * slope;
	
	outColor = vec4(grey, grey, grey, 1.0);
}
