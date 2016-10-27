function Box(options) {
  var selfId = 'box' + options.id;

  var newBox = $(
    '<div id="' + selfId + '" class="box">' +
    '<input type="text" name="statement" placeholder="Enter title"/>' +
    '<input type="number" name="baseValue" placeholder="Enter base value"/>' +
    '<button class="btn plus z-depth-0">' +
    '+</button>        ' +
    '<button class="btn minus z-depth-0">' +
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
        detachable: false,
        endpoint: 'Blank'
      });
    }
  });
}
