function Form() {
  var self = this;
  var id = 0;

  // Get the modal
  var modal = $('#formModal');

  // Get the button that opens the modal
  $('button').click(function() {
    $('#issueLabel').text(this.value);
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

    newBox.on('load', function(ci) {
      self.fire('load', ci);
    });

    $('#myForm').trigger('reset');
    modal.closeModal();
  });

  // When the user clicks anywhere outside of the modal, close it
  $(window).click(function(e) {
    if (e.target === modal) {
      modal.closeModal();
    }
  });
}

mixin(EventMixin, Form);
