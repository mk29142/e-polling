function Form(childId) {

  //TODO: creator chooses the order of the nodes, extra section on the form ???

  var self = this;
  var id = 0;
  if(childId) {
    id = childId;
  }

  // Get the modal
  var modal = $('#myModal');

  var type;

  // Get the button that opens the modal
  $('#issueButton').click(function() {
    type = this.value;
    $('#IssueLabel').text(this.value);
    modal.openModal();
  });

  $('#submit').click(function(e) {
    e.preventDefault();

    var issue = $('#issue').val();
    var baseValue = $('#baseValue').val();

    var newBox = new IssueBox({
      id: id++,
      issue: issue,
      baseValue: baseValue
    });

    //fires the connections when they occur to main to be printed out
    newBox.on('load', function(ci) {
      self.fire('load', ci);
    });

    $('#myForm').trigger('reset');
    $('#issueButton').hide();
    modal.closeModal();
  });

  // When the user clicks anywhere outside of the modal, close it
  $(window).click(function(e) {
    if (e.target == modal) {
      modal.closeModal();
    }
  });
}

mixin(EventMixin, Form);
