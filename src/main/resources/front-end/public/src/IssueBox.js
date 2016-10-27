function IssueBox(options) {

  var self = this;

  //Make the divs
  var newBox = this.createBox(options);

  //Make the plus and minus buttons do something, i.e. set up listeners
  this.createChildren(newBox);
}

/* Create the divs for the box itself and the +/- buttons on the box. */
IssueBox.prototype.createBox = function (options) {
  console.log('Creating a box');
  var newBox = $('<div/>', {
    id: 'box' + options.id,
    class: 'box',
    text: options.issue
  }).appendTo('#boxContainer');

  var proButton = $('<button/>', {
    class: 'button_add_pro btn',
    value: "Pro",
    text: '+'
  }).appendTo('#box' + options.id);

  var conButton = $('<button/>', {
    class: 'button_add_con btn',
    value: "Con",
    text: '-'
  }).appendTo('#box' + options.id);

  return newBox;
};

IssueBox.prototype.createChildren = function (newBox) {
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

      if ((type != 'Pro') && (type != 'Con')) {
        return;
      }

      console.log(type);
      var parentnode = $(child).parent();
      var modal = $('#myModal');
      $('#IssueLabel').text(this.value);
      modal.openModal();
      //add pro/con node
      // var conBox = new ConBox();
      // conBox.getId();
      //jsPlumb.connect (......);

      $('#submit').click(function(e) {
        e.preventDefault();

        var issue = $('#issue').val();
        var baseValue = $('#baseValue').val();

        $('#myForm').trigger('reset');
        modal.closeModal();
      });

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
    });
  });
};

mixin(EventMixin, IssueBox);
