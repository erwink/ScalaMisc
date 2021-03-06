package winnie

import scala.swing._
import util.Random.nextInt
import java.awt.{Dimension, Graphics2D, Color, Image, Toolkit, Font}
import javax.sound.sampled._
import java.io._

object VoorRuben extends SimpleSwingApplication {

  import scala.swing.event._
  import scala.swing.event.Key._
  var activeClips : List[Clip] = Nil
  var game = new Game()


  override def top = new MainFrame {
    title = "Ruben en Winnie"
    background = Color.lightGray

    contents = new Panel() {
      focusable = true
      preferredSize = new Dimension(700, 730)

      listenTo(keys)

      override def paint(g: Graphics2D) = game.draw(g)

      reactions += {
        case KeyPressed(_, Left, _, _) => step(West);
        case KeyPressed(_, Right, _, _) => step(East);
        case KeyPressed(_, Up, _, _) => step(North);
        case KeyPressed(_, Down, _, _) => step(South);
        case KeyPressed(_, Enter, _, _) if !game.active => game = new Game(); repaint
      }
    }

    def step(dir: Direction) {
      if (game.active)
        game.doMove(dir)
      repaint()
    }

  }
}

class Game {
  var winnie = Winnie.initialPos
  var steps = 0;


  def active = !honeypots.isEmpty


    val offset = Point(10, 30)
  // position top-left cell
  val cellSize = 40
  // width and height of a single cell
  val bordWidth = 17
  val bordHeight = 17

  var honeypots = {
    val limit = 4 + nextInt(8)
    for (i <- 1 until limit)
      yield Point(nextInt(bordWidth), nextInt(bordHeight))
  }

  VoorRuben.activeClips.foreach(_.stop)
  playSound("luvhunny")

  def dim = Point(bordWidth, bordHeight) * cellSize

  def cell(p: Point): Point = offset + p * cellSize

  def doMove(direction: Direction) {
    val p = direction.nextPoint(winnie)


    if (!(p.x < 0 || p.y < 0 || p.x >= bordWidth || p.y >= bordHeight)) {
      winnie = p
      steps += 1
    }

    val honeyFound = honeypots contains winnie

    if (honeyFound) {
      honeypots = honeypots.filter(_ != winnie)
      playSound("smack")
    } else {
      if(steps % 9 == 0)
        playSound("dumdum")
    }

    if(!active)
      playSound("rumblysong")

  }

  def draw(g: Graphics2D) {
    // draw cell area
    g.setColor(Color.white)
    g.fillRect(offset.x, offset.y, dim.x, dim.y)
    g.setColor(Color.black)
    g.drawRect(offset.x, offset.y, dim.x, dim.y)
    g.drawImage(Winnie.background, offset.x + 1, offset.y + 1, dim.x - 1, dim.y - 1, null)
    // draw objects
    drawHoneyPots(g)
    drawWinnie(g)

    if(!active) {
      g.setColor(Color.black)
      g.setFont(new Font("sansserif", Font.BOLD, 50))
      g.drawString("Ruben wint", 100, 200)
    }
  }

  def drawWinnie(g: Graphics2D) {
    drawImageOnCell(g, Winnie.img, winnie)
  }

  def drawHoneyPots(g: Graphics2D) {
    for (p <- honeypots)
      drawImageOnCell(g, Winnie.hunnyImg, p)
  }

  def drawImageOnCell(g: Graphics2D, img: Image, p: Point) = {
    val c = cell(p)
    g.drawImage(img, c.x, c.y, null)
  }

  def playSound(sound: String) {
    val clip = AudioSystem.getClip();
    val inputStream = AudioSystem.getAudioInputStream(new FileInputStream(sound + ".wav"));
    clip.open(inputStream);
    VoorRuben.activeClips ::= clip
    clip.start
  }


}


object Winnie {
  private val toolkit = Toolkit.getDefaultToolkit()
  val img = toolkit.getImage("winnie.png")
  val hunnyImg = toolkit.getImage("hunny.png")
  val background = toolkit.getImage("background.png")

  def initialPos: Point =
    Point(5, 5)
}

abstract sealed class Direction(val unit: Point) {
  def nextPoint(point: Point) = point + unit
}

case object North extends Direction(Point(0, -1))
case object East extends Direction(Point(1, 0))
case object South extends Direction(Point(0, 1))
case object West extends Direction(Point(-1, 0))

case class Point(x: Int, y: Int) {
  def +(that: Point): Point =
    Point(x + that.x, y + that.y)

  def *(n: Int): Point =
    Point(x * n, y * n)
}
