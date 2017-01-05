function Box(options) {
  let selfId = 'box' + options.id;
  let typeMessage = options.type === 'Pro' || options.type === 'Con' ?
    options.type + ' argument' : 'Enter Title';
  let currDelete;

  let newBox = $(
    '<div id="' + selfId + '" class="box ' +
    options.type + '" data-level="' + options.level + '">' +
    '<input type="text" name="statement" placeholder="' + typeMessage + '"/>' +
    '<button class="close close-button">' +
    '&times;</button>        ' +
    '<button class="btn plus z-depth-0">' +
    '+</button>        ' +
    '<button class="btn minus z-depth-0">' +
    '-</button>' +
    '</div>').appendTo('#boxContainer');

  $('.close-button').click(function(e) {
    currDelete = e.target.parentNode;

    if (confirm('Are you sure?')) {
      jsPlumb.detachAllConnections(currDelete);
      jsPlumb.removeAllEndpoints(currDelete);
      jsPlumb.detach(currDelete);
      currDelete.remove();
    }
  });

  $('#' + selfId + ' .plus').click(function(e) {
    let parentId = e.target.parentNode.id;

    let boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Pro'
    };

    list.push(boxOptions);
    new Box(boxOptions);
  });

  $('#' + selfId + ' .minus').click(function(e) {
    let parentId = e.target.parentNode.id;

    let boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Con'
    };

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
        endpoint: 'Blank',
      });
    }
  });
}
