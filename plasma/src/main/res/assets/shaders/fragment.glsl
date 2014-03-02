uniform sampler2D uTexture;

varying vec4 vColor;

void main() {
    gl_FragColor =  texture2D(uTexture, gl_PointCoord) * vColor;
}
