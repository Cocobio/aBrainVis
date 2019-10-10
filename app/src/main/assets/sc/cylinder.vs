#version 320 es
precision highp float;

in vec3 vertexPos;			// Posicion de las fibras
in int vertexCol;			// Colores

flat out int gs_color;

void main() {
	gs_color = int(vertexCol);
	gl_Position = vec4(vertexPos, 1.0);
}
