function Box(options) {
  var selfId = 'box' + options.id;

  var newBox = $(
    '<div id="' + selfId + '" class="box">' +
    '<input type="text" name="statement"/>' +
    '<input type="number" name="baseValue" />' +
    '<button class="btn plus">' +
    '+</button>' +
    '<button class="btn minus">' +
    '-</button>' +
    '</div>')
    .appendTo('#boxContainer');

  $('#' + selfId + ' .plus').click(function(e) {
    var parentId = e.target.parentNode.id;

    new Box({
      id: id++,
      parentId: parentId
    });
  });

  $('#' + selfId + ' .minus').click(function(e) {
    var parentId = e.target.parentNode.id;

    new Box({
      id: id++,
      parentId: parentId
    });
  });

  jsPlumb.ready(function() {
    jsPlumb.draggable(newBox);

    if (options.parentId) {
      jsPlumb.connect({
        source: selfId,
        target: options.parentId,
        anchors: ['Top', 'Bottom'],
        detachable: false
      });
    }
  });
}
