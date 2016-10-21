function IssueBox(options) {
  var self = this;

  var newBox = $('<div/>', {
    id: 'box' + options.id,
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  jsPlumb.ready(function() {
    jsPlumb.draggable(newBox);

    var parentEndPoint = {
      endpoint: 'Rectangle',
      isSource: true,
      isTarget: true,
      maxConnections: 1,
      anchor: 'Top'
    };

    var childEndPoint = {
      endpoint: 'Rectangle',
      isSource: true,
      isTarget: true,
      maxConnections: -1,
      anchor: 'Bottom'
    };

    var box = $('#box' + options.id);
    jsPlumb.addEndpoint(box, parentEndPoint);
    jsPlumb.addEndpoint(box, childEndPoint);

    jsPlumb.bind('connection', function(ci) {
      self.fire('load', ci.sourceId + '    ' + ci.targetId);
    });
  });
}

mixin(EventMixin, IssueBox);
