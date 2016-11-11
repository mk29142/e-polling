(function() {
  var pollId = window.location.href.substring(
    window.location.href.lastIndexOf('/') + 1);

  $.get('/boxes/' + pollId, function(data) {
    var questions = JSON.parse(data);

    var title = $('#debate-title');
    var question = $('#question');
    var options = $('input[name=options]');
    var reason = $('#opinion-area');
    var issue = questions[0];
    console.log(issue);
    var counter = 0;
    var currQ = questions[counter];

    $('#finalQ').hide();
    title.html(issue.text);
    question.html(currQ.text);
    setNavList();
    setActive();

    $('#nextQ').click(function(e) {
      e.preventDefault();
      changeQuestion();
    });

    $('#finalQ').click(function(e) {
      e.preventDefault();
//      createQuestion("Does this work");
      createQuestion("test1", 1);
      createQuestion("test2", 2);
      createQuestion("test3", 3);
//      $('#dynamicModal').openModal({
//        dismissible: false
//      });

      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      //Do a post to /user so we are able to log their ip address once to put questions in the database
      $.ajax({
         type: 'POST',
         url: '/user/'+pollId,
         dataType: 'json',
      });

      // Send back questions with ajax and redirect to results page
      var dynamicData = {
        questions: questions,
        currentHead: 0
      };

      var printCounter = 0

      do {
        $.ajax({
          type: 'POST',
          url: '/answers/'+pollId,
          data: JSON.stringify(dynamicData),
          dataType: 'json',
          success: function(data) {
            console.log("DATA is ");
            console.log(data);
            dynamicData.questions = data;
          }
        });

        //a modal will pop up with dynamic questions from data obj
        //on last round of dynamic questions modal will show submit
        //window.location.href = '/results/' + data;
        console.log("DYNAMIC DATA IS " + printCounter++);
        console.log(dynamicData);


        dynamicData.currentHead++;
      } while (dynamicData.currentHead < questions.length);
    });

    $('#nav-list .collection-item').click(function(e) {
      counter = e.target.attributes[1].value - 1;
      changeQuestion();
    });

    function setNavList() {
      var nav = $('#nav-list');

      for (var i = 0; i < questions.length; i++) {
        nav.append('<a class="collection-item"' +
        'value="' +
        questions[i].id +
        '">' +
        questions[i].text +
        '</a>');
      }
    };

    function setActive() {
      var nav = $('#nav-list');
      var children = nav.children();
      children.removeClass('active');

      var active = children.eq(counter);
      active.addClass('active');
    }

    function changeQuestion() {
      currQ.support = options.filter(':checked').val();
      currQ.reason = reason.val();

      currQ = questions[++counter];
      question.text(currQ.text);
      reason.val(currQ.reason);

      if (currQ.support) {
        $('#vote-yes').prop('checked', currQ.support === 'yes');
        $('#vote-no').prop('checked', currQ.support === 'no');
      } else {
        $('#vote-yes').prop('checked', false);
        $('#vote-no').prop('checked', false);
      }

      if (currQ.id === questions.length - 1) {
        $('#nextQ').hide();
        $('#finalQ').show();
      } else {
        $('#nextQ').show();
        $('#finalQ').hide();
      }

      setActive();
    }

    function createQuestion(question, counter) {
      var q = '<div id="' + question + '">' +
      '<p>' + question + '</p>' +
      '<input type="radio" id="q' + counter + '-yes" name="'+ question + '" value="yes">' +
      '<label for="q' + counter +'-yes">Yes</label>   &nbsp; &nbsp; &nbsp;  ' +
      '<input type="radio" id="q' + counter + '-no" name="'+ question + '" value="no">' +
      '<label for="q' + counter +'-no">No</label>' +
      '</div>'

      $('#questions').append(q);
    }
  });

})();
