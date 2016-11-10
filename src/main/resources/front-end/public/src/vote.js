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
    var counter = 1;
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

        $.ajax({
          type: 'POST',
          url: '/answers/'+pollId,
          data: JSON.stringify(dynamicData),
          dataType: 'json',
          success: function(data) {
//            console.log(data);
            dynamicData.questions = data;
            console.log(dynamicData);

            $('#conflictTitle').text('CONFLICT! Your answers to the following questions are inconsistent with the question: ' +
            dynamicData.questions[0].text + ". Please change your answer or give a reason as you why you answered the way you did.");
            for(var i = 1; i < dynamicData.questions.length; i++) {
              var support = dynamicData.questions[i].support;
              createQuestion(dynamicData.questions[i].text, i);
              $('#q' + i + "-" + support).prop('checked', true);
            }
          }
        });

        //a modal will pop up with dynamic questions from data obj
        //on last round of dynamic questions modal will show submit
        //window.location.href = '/results/' + data;

        $('#dynamicModal').openModal({
          dismissible: false
        });

        $('#dynamicQuestionSubmit').click(function(e) {
          e.preventDefault();
          if(dynamicData.currentHead < questions.length) {
           dynamicData.currentHead++;
           for(var i = 1; i < dynamicData.questions.length; i++) {
             var val = $('input[name=' + i + ']:checked', '#dynamicQuestionForm').val();
             dynamicData.questions[i].support = val;
            }
            $.ajax({
              type: 'POST',
              url: '/answers/'+pollId,
              data: JSON.stringify(dynamicData),
              dataType: 'json',
              success: function(data) {
                console.log(data);
                dynamicData.questions = data;



                $('#conflictTitle').text('CONFLICT! Your answers to the following questions are inconsistent with the question: ' +
                dynamicData.questions[0].text + ". Please change your answer or give a reason as you why you answered the way you did.");
                for(var i = 1; i < dynamicData.questions.length; i++) {
                  var support = dynamicData.questions[i].support;
                  createQuestion(dynamicData.questions[i].text, i);
                  $('#q' + i + "-" + support).prop('checked', true);
                }
                $('#dynamicModal').openModal({
                  dismissible: false
                });
              }
            });
          }

        });

        //
      //  dynamicData.currentHead++;
     // } while (dynamicData.currentHead < questions.length);
    });

    $('#nav-list .collection-item').click(function(e) {
      counter = e.target.attributes[1].value - 1;
      changeQuestion();
    });

    function setNavList() {
      var nav = $('#nav-list');

      for (var i = 1; i < questions.length; i++) {
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

      var active = children.eq(counter - 1); // Nav-list doesn't contain the issue.
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
