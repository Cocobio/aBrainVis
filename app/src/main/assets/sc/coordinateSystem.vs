#version 320 es

// Datos de entrada
in vec4 vertexPos;

// Datos de salida
out vec4 color;

uniform mat4 M[3];
uniform mat4 V;
uniform mat4 P;

uniform vec4 colorArray[3];

void main() {
    color = colorArray[gl_InstanceID];
    gl_Position = P*V*M[gl_InstanceID]*vertexPos;
}
