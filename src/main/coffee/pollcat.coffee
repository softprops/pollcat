window._admin = false
$ = jQuery
$ ->

  questions = ($ '#questions')

  renderQuestion = (q) ->
    id = q.id.split(':').pop()
    adminExtras = if window._admin then "<div> <a class='d' href='#'>☠</a> <a class='a' href='#'>?</a> </div>" else ""
    "<li class='q' id='q-#{id}' data-votes='#{q.votes}'>
      <div class='vcount'>#{q.votes}</div>
      <div class='txt'>#{q.text}</div>
      <div class='vote'>
        <div>
          <a class='y' href='#'>+</a>
          <a class='n' href='#'>-</a>
        </div>
        #{adminExtras}
      </div>
    </li>"

  questionAsked = (q) ->
    questions.prepend renderQuestion(q)
    resort()

  upvote = (id) -> chgvotes(id, 1)

  downvote = (id) -> chgvotes(id, -1)

  chgvotes = (id, n) ->
    el = $("#"+id)
    if el.length
      votes = parseInt(el.data().votes, 10) + n
      el.find('.vcount').text votes
      el.data().votes = votes
      el.attr("data-votes", votes)
    resort()

  resort = () ->
    container = $("#questions")
    qs = $(".q", container)
    qs.sort (a,b) -> parseInt($(b).data().votes, 10) - parseInt($(a).data().votes, 10)
    container.empty().append(qs)

  asking = (q) ->
    $("#asking").text($("#q-"+q).find(".txt").text())

  process = (m) ->
    [action, id, data] = m.split ':'
    if action is 'up' then upvote("q-#{id}")
    else if action is 'down' then downvote("q-#{id}")
    else if action is 'ask'
      questionAsked
        id: id,
        text: data,
        votes: 0
    else if action is 'curr' then asking(id)

  # upvote
  $(".y").live 'click', (e) ->
    e.preventDefault()
    self = $(@)
    q = self.parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2), v: 'up'), (e) ->
      return
    false

  # downvote
  $(".n").live 'click', (e) ->
    e.preventDefault()
    q = $(@).parent().parent().parent().attr("id")
    $.post '/votes', (poll: 'talk', q: q.substring(2),  v: 'down'), (e) ->
      return
    false

  # delete
  $(".d").live 'click', (e) ->
    e.preventDefault()
    really = confirm("You really sure you wanna do that?")
    if really
      self = $(@)
      q = self.parent().parent().parent().attr("id")
      $.post '/questions', (poll: 'talk', q: q.substring(2)), (e) ->
        ($("#" + q).fadeOut 'fast', (e) ->
          self.remove()
          return) if e.status is 200
        return
    false

  # currently asking
  $(".a").live "click", (e) ->
    e.preventDefault()
    q = $(@).parent().parent().parent().attr("id").substring(2)
    $.post('/current', (name: 'talk', q: q), (e) ->
      asking(q) if e.status is 200
    )
    false

  # voting options
  $(".q").live "mouseover mouseout", (e) ->
    if e.type is "mouseover" then $(".vote", @).show()
    else $(".vote", @).hide()

  # ask
  $("#ask").submit (e) ->
    e.preventDefault()
    if $.trim($("#q").val()).length > 0
      $.post "/polls", $(this).serialize(), (d) ->
        $("#q").val("")
    false


  # get all of a poll's questions
  $.get("/polls",
    name: 'talk',
    (qs) => questionAsked(q) for q in qs
  )

  $("#login").live 'click', (e) -> $(@).html "Redirecting to Meetup"

  # pushed questions and votes
  if window.WebSocket || window.MozWebSocket
    uri = "ws://#{window.location.host}/cat/talk"
    cat = if window.WebSocket then new WebSocket(uri) else new MozWebSocket(uri)
    cat.onmessage = (m) ->
      process m.data
      return
    #if 'onbeforeunload' in window then window.onbeforeunload (e) -> cat.close()
    return
  else
    alert 'Your browser is inferior and thus does not support the html5 standard for websockets'

  return