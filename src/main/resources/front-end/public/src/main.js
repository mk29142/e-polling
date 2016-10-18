(function() {

  var fst = jsPlumb.getInstance();

  fst.ready(function() {
    // jsPlumb.connect({
    //   source: "item_left",
    //   target: "item_right",
    //   endpoint: "Rectangle"
    // });
    fst.draggable("item_left");
    fst.draggable("item_right");

    var button = document.getElementById('issue');
    button.addEventListener("click", function() {
      var box = new Box(fst);
    });
  });
})();
