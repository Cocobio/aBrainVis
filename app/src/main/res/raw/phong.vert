// Atributos de entrada
attribute vec3 vPosition;
attribute vec3 vColor;
attribute vec3 vNormal;

// Matriz ModelView y Projection
uniform mat4 uMVMatrix;
uniform mat4 uPMatrix;

// Posicion de la luz
uniform vec3 light_pos;

// Material del modelo
uniform vec3 ka;
uniform vec3 kd;
uniform vec3 ks;
uniform float shininess;

// Intensidad de la luz
uniform vec3 la;
uniform vec3 ld;
uniform vec3 ls;

varying mediump vec4 Color;

void main() {
      vec4 P = uMVMatrix * vec4(vPosition, 1.0);

      vec3 N = normalize(mat3(uMVMatrix)*vNormal);
      vec3 L = normalize(light_pos - P.xyz);
      vec3 V = normalize(-P.xyz);
      vec3 R = reflect(-L, N);

      Color = vec4((la*ka) + (max(dot(N, L), 0.0)*kd*ld) + (pow(max(dot(R, V), 0.0), shininess)*ks*ls), 1.0) * vec4(vColor, 1.0);
      gl_Position = uPMatrix * P;
};