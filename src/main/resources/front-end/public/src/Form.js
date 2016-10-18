function Form() {

  var fst = jsPlumb.getInstance();

  // Get the modal
  var modal = document.getElementById('myModal');

  // Get the button that opens the modal
  var btn = document.getElementById("myBtn");

  // Get the <span> element that closes the modal
  // var span = document.getElementsByClassName("close")[0];

  // When the user clicks on the button, open the modal
  btn.onclick = function() {
      modal.style.display = 'block';
  }

  // When the user clicks on <span> (x), close the modal
  // span.onclick = function() {
  //     modal.style.display = "none";
  // }

  var submitButton = document.getElementById('submit');
  submitButton.addEventListener('click', function(e) {
    e.preventDefault();

    var issue = document.getElementById('issue').value;
    var baseValue = document.getElementById('baseValue').value

    var newBox = new IssueBox({issue: issue, baseValue: baseValue});

    modal.style.display = 'none';
  });

  // When the user clicks anywhere outside of the modal, close it
  window.onclick = function(event) {
      if (event.target == modal) {
          modal.style.display = 'none';
      }
  }
}
