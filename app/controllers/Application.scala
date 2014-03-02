package controllers

import play.api._
import play.api.mvc._
import models.Post
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import util.markdown.PegDown._

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
      } catch {case _ => new DateTime(file.lastModified()) }
      
      Post(
        metadata.getOrElse("title", "title"),
        metadata.getOrElse("tagline", "tagline"),
        file.getName(),
        date,
        content)
    }
    posts
  }
  
  def index = Action {
    Ok(views.html.index("")(getPosts()))
  }

  def post(id: String) = Action {
    val post = getPosts().filter(_.name == id)
    if(post.isEmpty) NotFound
    Ok(views.html.post(post(0)))
  }
  
  // TODO to be refacted
  def archive() = Action {
    val map = Application.getPosts().groupBy(_.date.getYear())
    		.mapValues(_.groupBy(_.date.getMonthOfYear())
    		    .mapValues(_.sortWith((a, b) => a.date isAfter b.date)))
    Ok(views.html.pages.archive(map))
  }

}