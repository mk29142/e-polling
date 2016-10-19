function IssueBox(options) {
  var issueInstance = jsPlumb.getInstance();

  var newBox = $('<div/>', {
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  issueInstance.ready(function() {
    issueInstance.draggable(newBox);
  });
}
