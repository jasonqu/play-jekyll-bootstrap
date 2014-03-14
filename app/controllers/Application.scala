package controllers

import play.api._
import play.api.mvc._
import models.Post
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import util.markdown.PegDown._
import play.api.i18n.Messages

object Application extends Controller {
  import java.io.File
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  
  def getPosts() = {
    val postFiles = recursiveListFiles(new File("_content/_posts")).filterNot(_.isDirectory)
    val posts = postFiles.map { file =>
      val (metadata, content) = processMdFile(file)
      
      val date = try { DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(file.getName().substring(0, 10))
      } catch {case _ : Throwable => new DateTime(file.lastModified()) }
      
      Post(
        metadata.getOrElse("title", "no title"),
        metadata.getOrElse("tagline", ""),
        file.getName(),
        metadata.getOrElse("category", ""),
        { val s = metadata.getOrElse("tags", "[]").trim()
          s.substring(1, s.length() - 1).split(",").map(_.trim()).toList
        },
        date,
        content)
    }
    posts.sortWith((a, b) => a.date.isAfter(b.date))
  }
  
  def index = Action {
    Ok(views.html.index("")(getPosts()))
  }

  def post(id: String) = Action {
    val post = getPosts().filter(_.name == id)
    if(post.isEmpty) NotFound
    Ok(views.html.post(post(0)))
  }
  
  def archive() = Action {
    val map = Application.getPosts().groupBy(_.date.getYear())
    		.mapValues(_.groupBy(_.date.getMonthOfYear())
    		    .mapValues(_.sortWith((a, b) => a.date isAfter b.date)))
    Ok(views.html.pages.archive(map))
  }

  def categories() = Action {
    val map = Application.getPosts().groupBy(_.category)
    Ok(views.html.pages.categories(map))
  }
  
  def pages() = Action {
    Ok(views.html.pages.pages())
  }

  def tags() = Action {
    import collection.mutable.{ HashMap, MultiMap, Set }
    val mm = new HashMap[String, Set[Post]] with collection.mutable.MultiMap[String, Post]
    for(post <- Application.getPosts()) {
      post.tags.foreach { tag =>
        mm.addBinding(tag, post)
      }
    }
    Ok(views.html.pages.tags(mm))
  }
  
  def atom = Action {
    Ok(util.xml.Xml.atom(getPosts())).as("text/xml")
  }
  
  def rss = Action {
    Ok(util.xml.Xml.rss(getPosts())).as("text/xml")
  }

  import play.api.Play.current
  def sitemap = Action {
    val pages = "archive.html" :: "atom.xml" :: "categories.html" :: "index.html" :: "pages.html" :: "rss.xml" :: "sitemap.txt" :: "tags.html" :: Nil
    val ps = pages.map { Messages("production_url") + "/" + _ +"\n" }.mkString

    val pts = getPosts().map { Messages("production_url") + "/post/" + _.name }.mkString("\n")
    Ok(ps + "\n" + pts).as("text/plain")
  }
}