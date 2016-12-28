class Node {
    final int group;
    final int id;
    final float radius;
    final float opacity;
    final String text;

    Node(
            final int group,
            final int id,
            final float radius,
            final float opacity,
            final String text) {
        this.group = group;
        this.id = id;
        this.radius = radius;
        this.opacity = opacity;
        this.text = text;
    }
}
