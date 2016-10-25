function IssueBox(options) {

  var self = this;

  var newBox = this.createBox(options);
  this.createChildren(newBox);
}

IssueBox.prototype.createBox = function (options) {
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

        // var newBox = new IssueBox({
        //   id: id++,
        //   issue: issue,
        //   baseValue: baseValue
        // });

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
