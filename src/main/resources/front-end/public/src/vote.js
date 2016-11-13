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
    var counter = 0;
    var currQ = questions[counter];

    var dynamicCounter;
    var currConflictSet;

    var dynamicData = {
            questions: [questions], //questions to update answers for, initialised to be all questions with head as first value
            nextLevel: 0 //next level to be searched for inconsistencies
    };


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

      // send this ajax post for first answers and receive inconsistencies from first level

        $.ajax({
          type: 'POST',
          url: '/answers/'+pollId,
          data: JSON.stringify(dynamicData),
          dataType: 'json',
          success: function(data) {
          console.log(data);

            if (data != "STOP") {
              dynamicData.questions = data.dynamicQuestions;
              dynamicData.nextLevel = data.nextLevel;

              dynamicCounter = 0;
              currConflictSet = dynamicData.questions[dynamicCounter];

              displayModal();

            } else {
              window.location.href = '/results/';
            }
          }
        });

        //a modal will pop up with dynamic questions from data obj
        //on last round of dynamic questions modal will show submit
        //window.location.href = '/results/' + data;


        $('#dynamicQuestionSubmit').click(function(e) {
        e.preventDefault();
            for(var i = 1; i < dynamicData.questions[dynamicCounter].length; i++) {
               var val = $('input[name=' + i + ']:checked', '#dynamicQuestionForm').val();
               dynamicData.questions[dynamicCounter][i].support = val;
            }
            dynamicCounter++;

            // send this ajax post when we want inconsistencies for next level
            if (dynamicCounter >= dynamicData.questions.length) {
              $.ajax({
                type: 'POST',
                url: '/answers/'+pollId,
                data: JSON.stringify(dynamicData),
                dataType: 'json',
                success: function(data) {
                  console.log(data);
                  if (data != "STOP") {
                    dynamicData.questions = data.dynamicQuestions;
                    dynamicData.nextLevel = data.nextLevel;

                    dynamicCounter = 0;
                    currConflictSet = dynamicData.questions[dynamicCounter];
                    displayModal();
                  } else {
                    window.location.href = '/results'
                  }

                }
              });
             } else {
               currConflictSet = dynamicData.questions[dynamicCounter];
               displayModal();
             }

        });
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

    function displayModal() {

    $("#questions").html("");

    $('#conflictTitle').text('CONFLICT! Your answers to the following questions are inconsistent with the question: ' +
                  currConflictSet[0].text + ". Please change your answer or give a reason as you why you answered the way you did.");
                  for(var i = 1; i < currConflictSet.length; i++) {
                    var support = currConflictSet[i].support;
                    createQuestion(currConflictSet[i].text, i);
                    $('#q' + i + "-" + support).prop('checked', true);
                  }

                   $('#dynamicModal').openModal({
                            dismissible: false
                   });
    }

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
      '<input type="radio" id="q' + counter + '-yes" name="' + counter + '" value="yes">' +
      '<label for="q' + counter +'-yes">Yes</label>   &nbsp; &nbsp; &nbsp;  ' +
      '<input type="radio" id="q' + counter + '-no" name="' + counter + '" value="no">' +
      '<label for="q' + counter +'-no">No</label>' +
      '</div>'

      $('#questions').append(q);
    }
  });

})();
