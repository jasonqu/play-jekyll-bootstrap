package controllers

import play.api._
import play.api.mvc._

import models.Post
import org.joda.time.DateTime

object Application extends Controller {
  import java.io.File
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  
  def index = Action {
    val postFiles = recursiveListFiles(new File("_posts")).filterNot(_.isDirectory)
    val posts = postFiles.map { file =>
      Post(
        "title",
        "tagline",
        file.getName(),
        new DateTime(),
        "content")
    }
    
//    import org.pegdown.PegDownProcessor      
//    val pro = new PegDownProcessor()
//    val lines = scala.io.Source.fromFile(new File("_page/index.md")).getLines().toList
//    val md = lines.drop(1).dropWhile{!_.startsWith("---")}
    
    Ok(views.html.index("")(posts))
  }

  def post(id: String) = Action {
    val postFiles = recursiveListFiles(new File("_posts"))
      .filterNot(_.isDirectory)
      .filter(_.getName().equals(id))
      
    if (postFiles.isEmpty) {
      NotFound
    }
    
    val posts = Some(postFiles(0)).map { file =>
      import org.pegdown.PegDownProcessor      
      val pro = new PegDownProcessor()
      val lines = scala.io.Source.fromFile(file).getLines().toList
      //val content = lines.mkString
      
      //lines.s
      val (metadata, md) = lines.drop(1).span{!_.startsWith("---")}
      
      
      Post(
        "title",
        "tagline",
        file.getName(),
        new DateTime(),
        pro.markdownToHtml(md.drop(1).mkString("\n")))
    }
    Ok(views.html.post(posts.get))
//    posts match {
//      Some(post) => Ok(views.html.post(post))
//      _ => NotFound
//    }
    
  }

}