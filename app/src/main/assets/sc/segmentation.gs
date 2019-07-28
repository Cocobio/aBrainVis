#version 320 es

layout(lines) in;
layout(line_strip, max_vertices = 2) out;

in vec3 vsNormal[];
in float vsColor[];
in float vsVertex2Fib[];

out vec4 color;

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
	vec3 Ka;				// Reflectividad ambiental.
	vec3 Kd;				// Reflectividad difusa.
	vec3 Ks;				// Reflectividad especular.
	float shininess;		// Coeficiente de reflexion especular.
};
uniform MaterialInfo Material;

uniform sampler1D colorTable;
// uniform usampler1D fiberValidator;
uniform usampler2D fiberValidator;
uniform float alpha;
uniform int maxTextureDim;

void main() {
	int intV2F = int(vsVertex2Fib[0]);
	ivec2 fValidFetcher = ivec2(intV2F%maxTextureDim, intV2F/maxTextureDim);

	bool valid = bool(texelFetch(fiberValidator, fValidFetcher, 0).r);

	vec4 baseColor = texelFetch(colorTable, int(vsColor[0]), 0);

	if (alpha > 1.5f) {
		if (valid)
			for (int i=0; i<gl_in.length(); i++) {
				vec4 p = V*M*gl_in[i].gl_Position;

				vec3 n = normalize(mat3(V*M)*vsNormal[i]);
				vec3 l = normalize(vec3(Light.pos - p));
				vec3 v = normalize(-p.xyz);
				vec3 r = reflect(-l, n);

				vec3 ambi = Light.La*Material.Ka;
				vec3 diff = Light.Ld*Material.Kd*max( dot(l, n), 0.0);
				vec3 spec = Light.Ls*Material.Ks*pow(max(dot(r,v), 0.0), Material.shininess);
					
				// Salidas.
				color = vec4(ambi + diff + spec, 1.0) * baseColor;
				gl_Position = P*V*systemScaleM*M*gl_in[i].gl_Position;
				EmitVertex();
			}
	}

	else if (!valid)
			for (int i=0; i<gl_in.length(); i++) {
				vec4 p = V*M*gl_in[i].gl_Position;

				vec3 n = normalize(mat3(V*M)*vsNormal[i]);
				vec3 l = normalize(vec3(Light.pos - p));
				vec3 v = normalize(-p.xyz);
				vec3 r = reflect(-l, n);

				vec3 ambi = Light.La*Material.Ka;
				vec3 diff = Light.Ld*Material.Kd*max( dot(l, n), 0.0);
				vec3 spec = Light.Ls*Material.Ks*pow(max(dot(r,v), 0.0), Material.shininess);
					
				// Salidas.
				vec3 c = (ambi + diff + spec) * baseColor.rgb;
				float grey = (c[0]+c[1]+c[2])/3;
				color = vec4(grey, grey, grey, alpha);
				gl_Position = P*V*systemScaleM*M*gl_in[i].gl_Position;
				EmitVertex();
			}

	EndPrimitive();
}