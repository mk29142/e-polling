function Box(type) {
  this.type = type;

  var newBox = document.createElement('div');
  document.body.append(newBox);
  newBox.className = 'box';
  var content = document.createTextNode("hi");
  newBox.append(content);
    jsPlumb.ready(function() {
      jsPlumb.draggable(newBox);
    });
}
