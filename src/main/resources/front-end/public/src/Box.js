function Box(options) {
  var selfId = 'box' + options.id;
  var typeMessage = options.type == undefined ? 'Enter Title' : options.type + ' argument';

  var newBox = $(
    '<div id="' + selfId + '" class="box">' +
    '<input type="text" name="statement" placeholder="' + typeMessage + '"/>' +
    '<input type="number" name="baseValue" placeholder="Enter base value"/>' +
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
      parentId: parentId,
      type: 'Pro'
    });
  });

  $('#' + selfId + ' .minus').click(function(e) {
    var parentId = e.target.parentNode.id;

    new Box({
      id: id++,
      parentId: parentId,
      type: 'Con'
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
