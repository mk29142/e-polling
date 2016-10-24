(function() {
    var form = new Form();
    /*var hierarchyMap = new Map();*/
    //name: "Poll" will be set later from name user inputted
    var jsonList = {};

    form.on('load', function(ci) {
      //Trying something different to hierarchyMap - a list of jsons inside a json!
      /*var mapKeys = Array.from(hierarchyMap.keys());
      if(!mapKeys.includes(ci.source)) {
        //ci.source is the parent, and ci.target is the child
        hierarchyMap.set(ci.source, [ci.target]);
      } else {
        if(!hierarchyMap.get(ci.source).includes(ci.target)){
          hierarchyMap.get(ci.source).push(ci.target);
        }
      }
      console.log(hierarchyMap);
      console.log("parent is " + ci.source + '\n' + "child is " + ci.target);*/


    });
})();
