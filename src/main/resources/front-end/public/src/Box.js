function Box(options) {
  var selfId = 'box' + options.id;
  var typeMessage = options.type === 'Pro' || options.type === 'Con' ?
    options.type + ' argument' : 'Enter Title';

  var newBox = $(
    '<div id="' + selfId + '" class="box ' + options.type + '">' +
    '<input type="text" name="statement" placeholder="' + typeMessage + '"/>' +
    '<button class="btn plus z-depth-0">' +
    '+</button>        ' +
    '<button class="btn minus z-depth-0">' +
    '-</button>' +
    '</div>')
    .appendTo('#boxContainer');

  $('#' + selfId + ' .plus').click(function(e) {
    var parentId = e.target.parentNode.id;
    var boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Pro'
    }

    list.push(boxOptions);

    new Box(boxOptions);
  });

  $('#' + selfId + ' .minus').click(function(e) {
    var parentId = e.target.parentNode.id;
    var boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Con'
    }

    list.push(boxOptions);

    new Box(boxOptions);
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
