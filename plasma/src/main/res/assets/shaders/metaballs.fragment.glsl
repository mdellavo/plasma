uniform sampler2D uTexture;

varying vec4 vColor;

void main() {
    vec4 color = texture2D(uTexture, gl_PointCoord) * vColor;
    gl_FragColor = color;
}
