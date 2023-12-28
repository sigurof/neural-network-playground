precision highp float;

varying vec2 coord2d;
varying vec3 fragColor;

void main(void) {
    if (dot(coord2d, coord2d) > 0.50) {
        discard;
//        gl_FragColor = vec4(fragColor, 1);
    } else {
//        gl_FragColor = vec4(fragColor, 0);
        gl_FragColor = vec4(fragColor, 1);
    }
}
