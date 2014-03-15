uniform sampler2D uTexture;

varying vec2 vTextureCoord;

void main() {
    vec4 color = texture2D(uTexture, vTextureCoord);
    gl_FragColor = color;
}
