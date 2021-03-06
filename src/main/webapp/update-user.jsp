<%@ page import="beans.User" %>
<%@ page import="sql.Sql" %>
<%@ page import="java.sql.ResultSet" %>

<%
	User u = (User) request.getSession().getAttribute("user");
	if(u == null) {
		// viteur non connecté, page interdite
		response.sendRedirect("/");
	}else{
%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="viewport"    content="width=device-width, initial-scale=1.0">
	<meta name="description" content="">
	<meta name="author"      content="Sergey Pozhilov (GetTemplate.com)">
	
	<title>Covid Mechant - Modifier profil</title>

	<link rel="shortcut icon" href="assets-template/images/gt_favicon.png">
	
	<link rel="stylesheet" media="screen" href="http://fonts.googleapis.com/css?family=Open+Sans:300,400,700">
	<link rel="stylesheet" href="assets-template/css/bootstrap.min.css">
	<link rel="stylesheet" href="assets-template/css/font-awesome.min.css">

	<!-- Custom styles for our template -->
	<link rel="stylesheet" href="assets-template/css/bootstrap-theme.css" media="screen" >
	<link rel="stylesheet" href="assets-template/css/main.css">

	<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
	<!--[if lt IE 9]>
    <script src="assets-template/js/html5shiv.js"></script>
    <script src="assets-template/js/respond.min.js"></script>
    <![endif]-->
</head>

<body>
	<!-- Fixed navbar -->
	<div class="navbar navbar-inverse navbar-fixed-top headroom" >
		<div class="container">
			<div class="navbar-header">
				<!-- Button for smallest screens -->
				<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse"><span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span> </button>
				<a class="navbar-brand" href="index.jsp">Covid Mechant</a>
			</div>
			<div class="navbar-collapse collapse">
				<ul class="nav navbar-nav pull-right">
					<li><a href="index.jsp">Accueil</a></li>
					<li><a href="activites.jsp">Activites</a></li>
					<%
						if(u != null && u.isAdmin()) {
							out.println("<li class='active'><a href=/AdminPannelServlet>Panneau Administrateur</a></li>");
						}
						Sql sql = new Sql();
						ResultSet notifications = sql.doRequest("SELECT * FROM notification WHERE id_user_dst = " + u.getId() + " ORDER BY id_notification DESC");
						ResultSet nbNotifications = sql.doRequest("SELECT COUNT(*) AS total FROM notification WHERE id_user_dst = " + u.getId() + " ORDER BY id_notification DESC");
						while (nbNotifications.next()) {
							out.println("<li class='dropdown'>" +
									"<a href='#' class='dropdown-toggle' data-toggle='dropdown'>Notifications (" + nbNotifications.getInt("total") + ")<b class='caret'></b></a>" +
									"<ul class='dropdown-menu'>");
							while (notifications.next()) {
								out.println("<li><a href='#'> " + notifications.getString("message") + "</a></li>");
							}
							out.println("</ul>" +
									"</li>");
						}
					%>
					<li><a href="profil.jsp">Profil</a></li>
					<li><a class="btn" href="/DeconnexionServlet">DECONNEXION</a></li>
				</ul>
			</div><!--/.nav-collapse -->
		</div>
	</div> 
	<!-- /.navbar -->

	<header id="head" class="secondary"></header>

	<!-- container -->
	<div class="container">

		<ol class="breadcrumb">
			<li><a href="index.html">Accueil</a></li>
			<li class="active">Modifier un profil</li>
		</ol>

		<div class="row">
			
			<!-- Article main content -->
			<article class="col-sm-offset-2 col-sm-8 maincontent">
				<header class="page-header">
					<%
						String login;
						if(request.getParameter("userToUpdate") != null) {
							// cas 1, le paramètre provient de la liste des utilisateurs
							login = request.getParameter("userToUpdate");
						}else{
							// cas 2, on vient de mettre à jour un utilisateur
							login = (String) request.getSession().getAttribute("userToUpdate");
						}
						User user = sql.getUser(login);
					%>

					<h1 class="page-title">profil de <%=user.getPrenom()%> <%=user.getNom()%></h1>


				</header>
				
				<p>
					Details de ce compte. Vous pouvez mettre a jour ces champs.
				</p>
				<br>
				<form action="/ModifierProfilUtilisateurServlet" method="post">
					<div class="row">
						<%
							if(request.getParameter("error") != null) {
								out.println("<div class=\"col-sm-12\">");
								out.println("<div class='alert alert-warning' role='alert'>");
								out.println(request.getParameter("error"));
								out.println("</div>");
								out.println("</div>");
							}
							if(request.getParameter("success") != null) {
								out.println("<div class=\"col-sm-12\">");
								out.println("<div class='alert alert-success' role='alert'>");
								out.println(request.getParameter("success"));
								out.println("</div>");
								out.println("</div>");
							}
						%>
						<div class="col-sm-6">
							<%
								out.println("<input name='nom' class='form-control' type='text' placeholder='Nom' value='" + user.getNom() + "'>");
							%>
						</div>
						<div class="col-sm-6">
							<%
								out.println("<input name='prenom' class='form-control' type='text' placeholder='Prenom' value='" + user.getPrenom() + "'>");
							%>
						</div>
					</div>
					<div class="row top-margin">
						<div class="col-sm-6">
							<%
								out.println("<input name='email' class='form-control' type='email' placeholder='Email' value='" + user.getMail() + "'>");
								out.println("<input type='hidden' name='old_mail' value='" + user.getMail() + "'/>");
							%>
						</div>
						<div class="col-sm-6">
							<%
								out.println("<input name='naissance' class='form-control' type='date' placeholder='Date de naissance' value='" + user.getBirthday() + "'>");
							%>
						</div>
					</div>
					<br>
					<div class="row">
						<div class="col-sm-4 text-right">
							<input class="btn btn-action" type="submit" value="Modifier les informations">
						</div>
					</div>
					</br>
				</form>

				<form action="/SupprimerProfilUtilisateurServlet" method="post">
					<div class="row">
						<div class="col-sm-4 text-right">
							<input type="hidden" name="id_user" value="<%=user.getId()%>" />
							<button class="btn btn-danger" type="submit">Supprimer le compte</button>
						</div>
					</div>
				</form>

			</article>
			<!-- /Article -->

		</div>
	</div>	<!-- /container -->

	</br>

	<!-- JavaScript libs are placed at the end of the document so the pages load faster -->
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
	<script src="assets-template/js/headroom.min.js"></script>
	<script src="assets-template/js/jQuery.headroom.min.js"></script>
	<script src="assets-template/js/template.js"></script>
	
	<!-- Google Maps -->
	<script src="https://maps.googleapis.com/maps/api/js?key=&amp;sensor=false&amp;extension=.js"></script> 
	<script src="assets-template/js/google-map.js"></script>
	

</body>
</html>

<%
	}
%>