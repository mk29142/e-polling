function Box(options) {
  var selfId = 'box' + options.id;
  var typeMessage = options.type === 'Pro' || options.type === 'Con' ?
    options.type + ' argument' : 'Enter Title';
  var currDelete;

  var newBox = $(
    '<div id="' + selfId + '" class="box ' + options.type + '" data-level="' + options.level + '">' +
    '<input type="text" name="statement" placeholder="' + typeMessage + '"/>' +
    '<button class="close close-button">' +
    '&times</button>        ' +
    '<button class="btn plus z-depth-0">' +
    '+</button>        ' +
    '<button class="btn minus z-depth-0">' +
    '-</button>' +
    '</div>')
    .appendTo('#boxContainer');

  $('.close-button').click(function(e) {
    currDelete = e.target.parentNode;

//    var connectionList = jsPlumb.getConnections(currDelete);
//    console.log(jsPlumb.getConnections("box0"));

    deleteCheckModal();

  });

  $('#' + selfId + ' .plus').click(function(e) {
    var parentId = e.target.parentNode.id;
    var parentLevel = e.target.parentNode.getAttribute('data-level');

    var boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Pro',
      level: ++parentLevel
    };

    list.push(boxOptions);

    new Box(boxOptions);
  });

  $('#' + selfId + ' .minus').click(function(e) {
    var parentId = e.target.parentNode.id;
    var parentLevel = e.target.parentNode.getAttribute('data-level');

    var boxOptions = {
      id: id++,
      parentId: parentId,
      type: 'Con',
      level: ++parentLevel
    };

    list.push(boxOptions);

    new Box(boxOptions);
  });

  function deleteCheckModal() {

    $('#modalTitle').text('Are you sure you want to delete this box?');

    $('#checkModal').openModal({
       dismissible: false
    });

  }

  $('#delete-check-yes').click(function(e) {

     jsPlumb.detachAllConnections(currDelete);
     jsPlumb.removeAllEndpoints(currDelete);
     jsPlumb.detach(currDelete);
     currDelete.remove();

  });

  $('#delete-check-no').click(function(e) { });


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
