function IssueBox(options) {
  var issueInstance = jsPlumb.getInstance();

  var newBox = $('<div/>', {
    id: options.id,
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  issueInstance.ready(function() {
    issueInstance.batch(function () {
      issueInstance.bind('connection', function (info, e) {
        updateConnections(info.connection);
      });

      issueInstance.bind('connectionDetached', function (info, e) {
        updateConnections(info.connection, true);
      });

      issueInstance.bind('connectionMoved', function (info, e) {
        updateConnections(info.connection, true);
      });

      var dropOptions = {
        tolerance: 'touch',
        hoverClass: 'dropHover',
        activeClass: 'dragActive'
      };
      var color = '#00F';
      var endpoint = {
        endpoint: 'Rectangle',
        paintStyle: { width: 25, height: 21, fill: color },
        isSource: true,
        reattach: true,
        scope: 'blue',
        connectorStyle: {
          gradient: {
            stops: [
              [0, color],
              [0.5, '#09098e'],
              [1, color]
            ]
          },
          strokeWidth: 5,
          stroke: color
        },
        isTarget: true,
        dropOptions: dropOptions
      };

      issueInstance.addEndpoint('boxContainer', { anchor: [0, 1, 0, 1] }, endpoint);
    });

    issueInstance.draggable(newBox);
  });
}
