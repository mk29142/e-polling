function IssueBox(options) {

  var self = this;

  var newBox = $('<div/>', {
    id: 'box' + options.id,
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  var addButton = $('<button/>', {
    class: 'button_add_pro btn',
    value: "Pro",
    text: '+'
  }).appendTo('#box' + options.id);

  var addButton = $('<button/>', {
    class: 'button_add_con btn',
    value: "Con",
    text: '-'
  }).appendTo('#box' + options.id);

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

    //An issue box is never going to be a child in this function - this is
    //because this function only happens for the child after a button inside a
    //box has been clicked
    // Add additional anchor
    $('button').on('click', function () {
      var child = this;
      var type = child.value;
      console.log(type);
      var parentnode = $(child).parent();

      //add pro/con node
      // var conBox = new ConBox();
      // conBox.getId();
      //jsPlumb.connect (......);

      jsPlumb.addEndpoint(
        parentnode,
        parentEndPoint
      );

      jsPlumb.addEndpoint(
        parentnode,
        childEndPoint
      );
    });

    jsPlumb.bind('connection', function(ci) {
      //fire connections to form maybe using eventmixin
      if('box' + options.id == ci.sourceId) {
        self.fire('load', {
          source: ci.sourceId,
          target: ci.targetId
        });
      }
    })
  });
}

mixin(EventMixin, IssueBox);
