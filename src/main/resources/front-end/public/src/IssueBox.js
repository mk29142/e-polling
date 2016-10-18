function IssueBox(options) {

  var newBox = document.createElement('div');
  document.body.append(newBox);
  newBox.className = 'box';
  var content = document.createTextNode(options.issue);
  newBox.append(content);
  jsPlumb.ready(function() {
      jsPlumb.draggable(newBox);
  });
}
