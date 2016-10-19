function Form() {
  // Get the modal
  var modal = $('#myModal');

  // Get the button that opens the modal
  $('#myBtn').click(function() {
    modal.show();
  });

  $('#submit').click(function(e) {
    e.preventDefault();

    var issue = $('#issue').val();
    var baseValue = $('#baseValue').val();

    var newBox = new IssueBox({
      issue: issue,
      baseValue: baseValue
    });

    $('#myForm').trigger('reset');
    modal.hide();
  });

  // When the user clicks anywhere outside of the modal, close it
  $(window).click(function(e) {
    if (e.target == modal) {
      modal.hide();
    }
  });
}
