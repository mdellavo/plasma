uniform mat4 uMatrix;

attribute vec3 aPosition;
attribute vec2 aTextureCoord;

varying vec2 vTextureCoord;

void main() {
    gl_Position = uMatrix * vec4(aPosition, 1);
    vTextureCoord = aTextureCoord;
}
