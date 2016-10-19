function IssueBox(options) {
  var newBox = $('<div/>', {
    id: 'box' + options.id,
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  var addButton = $('<button/>', {
    class: 'button_add btn',
    text: 'Add'
  }).appendTo('#box' + options.id);

  jsPlumb.ready(function() {
    jsPlumb.draggable(newBox);

    var anEndpointDestination = {
      endpoint: 'Rectangle',
      isSource: true,
      isTarget: true,
      maxConnections: 1,
      anchor: 'Left'
    };

    // Add additional anchor
    $('.button_add').on('click', function () {
      var parentnode = $(this).parent();

      jsPlumb.addEndpoint(
        parentnode,
        anEndpointDestination
      );
    });
  });
}
