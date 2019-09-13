#version 320 es

#define cylinderNFace 7
// cylinderMaxVertex = cylinderNFace*2+2
#define cylinderMaxVertex 16

layout(lines) in;
layout(triangle_strip, max_vertices = cylinderMaxVertex) out;

flat in int gs_color[];

out vec4 color;

uniform vec2[cylinderNFace+1] cylinderVertex;

uniform sampler2D colorTable;

uniform mat4 systemScaleM;
uniform mat4 M;
uniform mat4 V;
uniform mat4 P;

struct LightInfo {
	vec4 pos;				// Posicion de la luz.
	vec3 La;				// Intensidad de luz ambiental.
	vec3 Ld;				// Intensidad de luz difusa.
	vec3 Ls;				// Intensidad de luz especular.
};
uniform LightInfo Light;

struct MaterialInfo {
	float Ka;				// Reflectividad ambiental.
	float Kd;				// Reflectividad difusa.
	float Ks;				// Reflectividad especular.
	float shininess;		// Coeficiente de reflexion especular.
};
uniform MaterialInfo Material;


vec4 multiplyQQ(vec4 leftQuaternion, vec4 rightQuaternion) {
	float lQw = leftQuaternion.x, lQx = leftQuaternion.y, lQy = leftQuaternion.z, lQz = leftQuaternion.w;
	float rQw = rightQuaternion.x, rQx = rightQuaternion.y, rQy = rightQuaternion.z, rQz = rightQuaternion.w;

	return vec4(lQw*rQw - lQx*rQx - lQy*rQy - lQz*rQz,
				lQx*rQw + lQw*rQx - lQz*rQy + lQy*rQz,
				lQy*rQw + lQz*rQx + lQw*rQy - lQx*rQz,
				lQz*rQw - lQy*rQx + lQx*rQy + lQw*rQz);
}


vec4 identityQ() {
	return vec4(1f, 0f, 0f, 0f);
}


vec4 quaternionFromAngleAndAxis(float angle, vec3 axis) {
	float module = length(axis);
	float s = sin(angle/2.0f) / module;

	return vec4(cos(angle/2.0f), axis.x*s, axis.y*s, axis.z*s);
}


vec4 quaternionFromAngleAndAxis(float angle, float x, float y, float z) {
	float module = sqrt(x*x + y*y + z*z);
	float s = sin(angle/2.0f) / module;

	return vec4(cos(angle/2.0f), x*s, y*s, z*s);
}


vec4 quaternionFromElements(float w, float x, float y, float z) {
	return vec4(w, x, y, z);
}


vec4 quaternionFromQuaternion(vec4 fromQuaternion) {
	return vec4(fromQuaternion);
}


vec4 normalizeQ(vec4 quaternion, int offset) {
	float module = length(quaternion);

	return vec4(quaternion / module);
}


vec4 invertQ(vec4 fromQuaternion) {
	return vec4(fromQuaternion.x, -fromQuaternion.yzw);
}


vec3 rotateVec3(vec3 vector, vec4 quaternion) {
	vec4 p = quaternionFromElements(0.0f, vector.x, vector.y, vector.z);
	vec4 invertedQuaternion = invertQ(quaternion);
	vec4 res = multiplyQQ(p, invertedQuaternion);
	res = multiplyQQ(quaternion, res);

	return res.yzw;
}


vec4 rotateVec4(vec4 vector, vec4 quaternion) {
	vec4 p = quaternionFromElements(0.0f, vector.x, vector.y, vector.z);
	vec4 invertedQuaternion = invertQ(quaternion);
	vec4 res = multiplyQQ(p, invertedQuaternion);
	res = multiplyQQ(quaternion, res);

	return vec4(res.yzw, vector.x);
}


mat3 rotationMat3(vec4 quaternion){
	float xx = quaternion.y*quaternion.y;
	float yy = quaternion.z*quaternion.z;
	float zz = quaternion.w*quaternion.w;
	float xy = quaternion.y*quaternion.z;
	float wz = quaternion.x*quaternion.w;
	float xz = quaternion.y*quaternion.w;
	float wy = quaternion.x*quaternion.z;
	float yz = quaternion.z*quaternion.w;
	float wx = quaternion.x*quaternion.y;

	return mat3(1.0-2.0*(yy+zz),	2.0*(xy+wz),	2.0*(xz-wy),
				2.0*(xy-wz),		1.0-2.0*(xx+zz),2.0*(yz+wx),
				2.0*(xz+wy),		2.0*(yz-wx),	1.0-2.0*(xx+yy));
}


mat4 rotationMat4(vec4 quaternion){
	float xx = quaternion.y*quaternion.y;
	float yy = quaternion.z*quaternion.z;
	float zz = quaternion.w*quaternion.w;
	float xy = quaternion.y*quaternion.z;
	float wz = quaternion.x*quaternion.w;
	float xz = quaternion.y*quaternion.w;
	float wy = quaternion.x*quaternion.z;
	float yz = quaternion.z*quaternion.w;
	float wx = quaternion.x*quaternion.y;

	return mat4(1.0-2.0*(yy+zz),	2.0*(xy+wz),	2.0*(xz-wy),	0.0,
				2.0*(xy-wz),		1.0-2.0*(xx+zz),2.0*(yz+wx),	0.0,
				2.0*(xz+wy),		2.0*(yz-wx),	1.0-2.0*(xx+yy),0.0,
				0.0,				0.0,			0.0,			1.0);
}


void main() {
	vec4 baseColor = texelFetch(colorTable, ivec2(int(gs_color[0]), 0), 0);

	vec4 point0 = gl_in[0].gl_Position;
	vec4 point1 = gl_in[1].gl_Position;

	vec4 line = point1 - point0;
	vec3 cylinderAxis = vec3(1.0, 0.0, 0.0);

	float angle = acos(dot(cylinderAxis, line.xyz)/(length(cylinderAxis)*length(line.xyz)));
	vec3 axis = cross(cylinderAxis, line.xyz);

	mat4 lineDirection = rotationMat4(quaternionFromAngleAndAxis(angle, axis));
	vec4 radiusDelta;

	for (int i=0; i<cylinderNFace+1; i++) {
		radiusDelta = lineDirection*vec4(0.0, cylinderVertex[i], 0.0);

		// Illumination.
		vec4 p = V*M*(point0+radiusDelta);
		vec3 n = normalize(mat3(V*M)*radiusDelta.xyz);
		vec3 l = normalize(vec3(Light.pos - p));
		vec3 v = normalize(-p.xyz);
		vec3 r = reflect(-l, n);

		vec3 ambi = Light.La*Material.Ka;
		vec3 diff = Light.Ld*Material.Kd*max( dot(l, n), 0.0);
		vec3 spec = Light.Ls*Material.Ks*pow(max(dot(r,v), 0.0), Material.shininess);

		// Salidas.
		color = vec4(ambi + diff + spec, 1.0) * baseColor;
		gl_Position = P*V*systemScaleM*M*(radiusDelta+point0);
		EmitVertex();


		p = V*M*(point1+radiusDelta);
		l = normalize(vec3(Light.pos - p));
		v = normalize(-p.xyz);
		r = reflect(-l, n);

		diff = Light.Ld*Material.Kd*max( dot(l, n), 0.0);
		spec = Light.Ls*Material.Ks*pow(max(dot(r,v), 0.0), Material.shininess);

		color = vec4(ambi + diff + spec, 1.0) * baseColor;
		gl_Position = P*V*systemScaleM*M*(radiusDelta+point1);
		EmitVertex();
	}
}

mat4 rotationMatrix(vec3 axis, float angle);
mat3 rotationMatrixFromAtoB(vec3 a, vec3 b);

mat4 rotationMatrix(vec3 axis, float angle) {
	axis = normalize(axis);

	float s = sin(angle);
	float c = cos(angle);
	float oc = 1.0 - c;

	return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
				oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
				oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
				0.0,                                0.0,                                0.0,                                1.0);
}


mat3 rotationMatrixFromAtoB(vec3 a, vec3 b) {
	vec3 v = cross(a, b);
	float s = length(v);
	float c = dot(a, b);
	mat3 vx = mat3( 0,   -v.z,    v.y,
				  v.z,      0,   -v.x,
				 -v.y,    v.x,      0);

	return mat3(1) + vx + vx*vx*(1.0-c)/(s*s);
}