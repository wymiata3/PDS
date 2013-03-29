/**
 * *******************************************************************
 *
 * $Id: pic24config.php 9061 2012-12-11 11:08:17Z mvuilleu $
 *
 * Implements yFindDisplayLayer(), the high-level API for DisplayLayer functions
 *
 * - - - - - - - - - License information: - - - - - - - - -
 *
 * Copyright (C) 2011 and beyond by Yoctopuce Sarl, Switzerland.
 *
 * 1) If you have obtained this file from www.yoctopuce.com, Yoctopuce Sarl
 * licenses to you (hereafter Licensee) the right to use, modify, copy, and
 * integrate this source file into your own solution for the sole purpose of
 * interfacing a Yoctopuce product with Licensee's solution.
 *
 * The use of this file and all relationship between Yoctopuce and Licensee are
 * governed by Yoctopuce General Terms and Conditions.
 *
 * THE SOFTWARE AND DOCUMENTATION ARE PROVIDED 'AS IS' WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL LICENSOR BE LIABLE FOR ANY INCIDENTAL,
 * SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST
 * OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY OR SERVICES, ANY CLAIMS BY
 * THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), ANY CLAIMS
 * FOR INDEMNITY OR CONTRIBUTION, OR OTHER SIMILAR COSTS, WHETHER ASSERTED ON
 * THE BASIS OF CONTRACT, TORT (INCLUDING NEGLIGENCE), BREACH OF WARRANTY, OR
 * OTHERWISE.
 *
 * 2) If your intent is not to interface with Yoctopuce products, you are not
 * entitled to use, read or create any derived material from this source file.
 *
 ********************************************************************
 */
package com.yoctopuce.YoctoAPI;

/**
 * YDisplayLayer Class: Image layer containing data to display
 *
 * A DisplayLayer is an image layer containing objects to display (bitmaps,
 * text, etc.). The content will only be displayed when the layer is active on
 * the screen (and not masked by other overlapping layers).
 */
public class YDisplayLayer {

    //--- (generated code: definitions)
    public enum ALIGN {
        TOP_LEFT (0),
        CENTER_LEFT (1),
        BASELINE_LEFT (2),
        BOTTOM_LEFT (3),
        TOP_CENTER (4),
        CENTER (5),
        BASELINE_CENTER (6),
        BOTTOM_CENTER (7),
        TOP_DECIMAL (8),
        CENTER_DECIMAL (9),
        BASELINE_DECIMAL (10),
        BOTTOM_DECIMAL (11),
        TOP_RIGHT (12),
        CENTER_RIGHT (13),
        BASELINE_RIGHT (14),
        BOTTOM_RIGHT (15);
        public final int value;
        private ALIGN(int val) 
        {
            this.value = val;
        };
    };
    
    //--- (end of generated code: definitions)

    private YDisplay _display;
    private String _id;
    private StringBuilder _cmdbuff;
    private Boolean _hidden;

    // internal function to flush any pending command for this layer
    public synchronized int flush_now()  throws YAPI_Exception
    {
        int res = YAPI.SUCCESS;
        if(_cmdbuff.length() > 0) {
            res = this._display.set_command(this._cmdbuff.toString());
            _cmdbuff.setLength(0);
        }
        return res;
    }
    
    // internal function to buffer a command for this layer
    private synchronized int command_push(String cmd)  throws YAPI_Exception
    {
        int res = YAPI.SUCCESS;
        
        if(_cmdbuff.length() + cmd.length() >= 100) {
            // force flush before, to prevent overflow
            res = this.flush_now();
        }
        if(_cmdbuff.length() == 0) {
            // always prepend layer ID first
            _cmdbuff.append(this._id);
        } 
        _cmdbuff.append(cmd);
        return res;
    }

    // internal function to send a command for this layer
    private synchronized int command_flush(String cmd)  throws YAPI_Exception
    {
        int res = this.command_push(cmd);
        if(_hidden) {
            return res;
        }
        return this.flush_now();
    }

    public YDisplayLayer(YDisplay parent, String id)
    {
        this._display = parent;
        this._id = id;
        this._cmdbuff = new StringBuilder(128);
        this._hidden = false;
    }

    //--- (generated code: YDisplayLayer implementation)

    /**
     * Revert the layer to its initial state (fully transparent, default settings).
     * Reinitialize the drawing pointer to the upper left position,
     * and select the most visible pen color. If you only want to erase the layer
     * content, use the method clear() instead.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int reset()  throws YAPI_Exception
    {
        return command_flush(String.format("X"));
    }

    /**
     * Erase the whole content of the layer (make it fully transparent).
     * This method does not change any other attribute of the layer.
     * To reinitialize the layer attributes to defaults settings, use the method
     * reset() instead.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int clear()  throws YAPI_Exception
    {
        return command_flush(String.format("x"));
    }

    /**
     * Select the pen color for all subsequent drawing functions,
     * including text drawing. The pen color is provided as a RGB value.
     * For grayscale or monochrome displays, the value will
     * automatically be converted to the proper range.
     * 
     * @param color: the desired pen color, as a 24-bit RGB value
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int selectColorPen(int color)  throws YAPI_Exception
    {
        return command_push(String.format("c%06x",color));
    }

    /**
     * Select the pen gray level for all subsequent drawing functions,
     * including text drawing. The gray level is provided as a number between
     * 0 (black) and 255 (white, or whichever the lighest color is).
     * For monochrome displays (without gray levels), any value
     * lower than 128 will be rendered as black, and any value equal
     * or above to 128 will be non-black.
     * 
     * @param graylevel: the desired gray level, from 0 to 255
     * 
     * @return YAPI.SUCCESS si l'opération se déroule sans erreur.
     * 
     * En cas d'erreur, déclenche une exception ou retourne un code d'erreur négatif.
     */
    public int selectGrayPen(int graylevel)  throws YAPI_Exception
    {
        return command_push(String.format("g%d",graylevel));
    }

    /**
     * Select an eraser instead of a pen for all subsequent drawing functions,
     * except for text drawing and bitmap copy functions. Any point drawn
     * using the eraser will become transparent (as when the layer is empty),
     * showing the other layers beneath it.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int selectEraser()  throws YAPI_Exception
    {
        return command_push(String.format("e"));
    }

    /**
     * Enable or disable anti-aliasing for drawing oblique lines and circles.
     * Anti-aliasing provides a smoother aspect when looked from enough distance,
     * but it can add fuzzyness when the display is looked from very near.
     * At the end of the day, it is your personal choice.
     * Anti-aliasing is enabled by default on grayscale and color displays,
     * but you can disable it if you prefer. This setting has no effect
     * on monochrome displays.
     * 
     * @param mode: <t>true</t> to enable antialiasing, <t>false</t> to
     *         disable it.
     * 
     * @return YAPI.SUCCESS si l'opération se déroule sans erreur.
     * 
     * En cas d'erreur, déclenche une exception ou retourne un code d'erreur négatif.
     */
    public int setAntialiasingMode(boolean mode)  throws YAPI_Exception
    {
        return command_push(String.format("a%d",mode));
    }

    /**
     * Draw a single pixel at a specified position.
     * 
     * @param x: the distance from left of layer, in pixels
     * @param y: the distance from top of layer, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawPixel(int x,int y)  throws YAPI_Exception
    {
        return command_flush(String.format("P%d,%d",x,y));
    }

    /**
     * Draw an empty rectangle at a specified position.
     * 
     * @param x1: the distance from left of layer to the left border of the rectangle, in pixels
     * @param y1: the distance from top of layer to the top border of the rectangle, in pixels
     * @param x2: the distance from left of layer to the right border of the rectangle, in pixels
     * @param y2: the distance from top of layer to the bottom border of the rectangle, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawRect(int x1,int y1,int x2,int y2)  throws YAPI_Exception
    {
        return command_flush(String.format("R%d,%d,%d,%d",x1,y1,x2,y2));
    }

    /**
     * Draw a filled rectangular bar at a specified position.
     * 
     * @param x1: the distance from left of layer to the left border of the rectangle, in pixels
     * @param y1: the distance from top of layer to the top border of the rectangle, in pixels
     * @param x2: the distance from left of layer to the right border of the rectangle, in pixels
     * @param y2: the distance from top of layer to the bottom border of the rectangle, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawBar(int x1,int y1,int x2,int y2)  throws YAPI_Exception
    {
        return command_flush(String.format("B%d,%d,%d,%d",x1,y1,x2,y2));
    }

    /**
     * Draw an empty circle at a specified position.
     * 
     * @param x: the distance from left of layer to the center of the circle, in pixels
     * @param y: the distance from top of layer to the center of the circle, in pixels
     * @param r: the radius of the circle, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawCircle(int x,int y,int r)  throws YAPI_Exception
    {
        return command_flush(String.format("C%d,%d,%d",x,y,r));
    }

    /**
     * Draw a filled disc at a given position.
     * 
     * @param x: the distance from left of layer to the center of the disc, in pixels
     * @param y: the distance from top of layer to the center of the disc, in pixels
     * @param r: the radius of the disc, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawDisc(int x,int y,int r)  throws YAPI_Exception
    {
        return command_flush(String.format("D%d,%d,%d",x,y,r));
    }

    /**
     * Select a font to use for the next text drawing functions, by providing the name of the
     * font file. You can use a built-in font as well as a font file that you have previously
     * uploaded to the device built-in memory. If you experience problems selecting a font
     * file, check the device logs for any error message such as missing font file or bad font
     * file format.
     * 
     * @param fontname: the font file name
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int selectFont(String fontname)  throws YAPI_Exception
    {
        return command_push(String.format("&%s%c",fontname,27));
    }

    /**
     * Draw a text string at the specified position. The point of the text that will be aligned
     * to the specified pixel position is called the anchor point, and can be chosen among
     * several options. Text is rendered from left to right, without implicit wrapping.
     * 
     * @param x: the distance from left of layer to the text ancor point, in pixels
     * @param y: the distance from top of layer to the text ancor point, in pixels
     * @param anchor: the text anchor point, chosen among the Y_ALIGN enumeration:
     *         TOP_LEFT,    CENTER_LEFT,    BASELINE_LEFT,    BOTTOM_LEFT,
     *         TOP_CENTER,  CENTER,         BASELINE_CENTER,  BOTTOM_CENTER,
     *         TOP_DECIMAL, CENTER_DECIMAL, BASELINE_DECIMAL, BOTTOM_DECIMAL,
     *         TOP_RIGHT,   CENTER_RIGHT,   BASELINE_RIGHT,   BOTTOM_RIGHT.
     * @param text: the text string to draw
     * 
     * @return YAPI.SUCCESS si l'opération se déroule sans erreur.
     * 
     * En cas d'erreur, déclenche une exception ou retourne un code d'erreur négatif.
     */
    public int drawText(int x,int y,ALIGN anchor,String text)  throws YAPI_Exception
    {
        return command_flush(String.format("T%d,%d,%d,%s%c",x,y,anchor.value,text,27));
    }

    /**
     * Move the drawing pointer of this layer to the specified position.
     * 
     * @param x: the distance from left of layer, in pixels
     * @param y: the distance from top of layer, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int moveTo(int x,int y)  throws YAPI_Exception
    {
        return command_push(String.format("@%d,%d",x,y));
    }

    /**
     * Draw a line from current drawing pointer position to the specified position.
     * The specified destination pixel is included in the line. The pointer position
     * is then moved to the end point of the line.
     * 
     * @param x: the distance from left of layer to the end point of the line, in pixels
     * @param y: the distance from top of layer to the end point of the line, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int lineTo(int x,int y)  throws YAPI_Exception
    {
        return command_flush(String.format("-%d,%d",x,y));
    }

    /**
     * Output a message in the console area, and advance the console pointer accordingly.
     * The console pointer position is automatically moved to the beginning
     * of the next line when a newline character is met, or when the right margin
     * is hit. When the new text to display extends below the lower margin, the
     * console area is automatically scrolled up.
     * 
     * @param text: the message to display
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int consoleOut(String text)  throws YAPI_Exception
    {
        return command_flush(String.format("!%s%c",text,27));
    }

    /**
     * Setup display margins for the consoleOut function.
     * 
     * @param x1: the distance from left of layer to the left margin, in pixels
     * @param y1: the distance from top of layer to the top margin, in pixels
     * @param x2: the distance from left of layer to the right margin, in pixels
     * @param y2: the distance from top of layer to the bottom margin, in pixels
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setConsoleMargins(int x1,int y1,int x2,int y2)  throws YAPI_Exception
    {
        return command_push(String.format("m%d,%d,%d,%d",x1,y1,x2,y2));
    }

    /**
     * Blank the console area within console margins, and reset the console pointer
     * to the upper left corner of the console.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int clearConsole()  throws YAPI_Exception
    {
        return command_flush(String.format("^"));
    }

    /**
     * Draw a bitmap image at current pointer position. The image must have been previously
     * uploaded to the device built-in memory. If you experience problems using an image
     * file, check the device logs for any error message such as missing image file or bad
     * image file format.
     * 
     * @param imagename: the image file name
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int drawImage(String imagename)  throws YAPI_Exception
    {
        return command_flush(String.format("*%s%c",imagename,27));
    }

    /**
     * Set the position of the layer relative to the display upper left corner.
     * When smooth scrolling is used, the display offset of the layer will be
     * automatically updated during the next milliseconds to animate the move of the layer.
     * 
     * @param x: the distance from left of display to the upper left corner of the layer
     * @param y: the distance from top of display to the upper left corner of the layer
     * @param scrollTime: number of milliseconds to use for smooth scrolling, or
     *         0 if the scrolling should be immediate.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int setLayerPosition(int x,int y,int scrollTime)  throws YAPI_Exception
    {
        return command_flush(String.format("#%d,%d,%d",x,y,scrollTime));
    }

    /**
     * Hide the layer. The state of the layer is perserved but the layer will not be displayed
     * on the screen up to the next call to unhide(). Hiding the layer can positively
     * affect the drawing speed, since it postpones the rendering until all operations are
     * completed (double-buffering).
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int hide()  throws YAPI_Exception
    {
        command_push(String.format("h")); 
        _hidden = true; 
        return flush_now(); 
        
    }

    /**
     * Show the layer. Show again the layer after a hide command.
     * 
     * @return YAPI.SUCCESS if the call succeeds.
     * 
     * @throws YAPI_Exception
     */
    public int unhide()  throws YAPI_Exception
    {
        _hidden = false; 
        return command_flush(String.format("s")); 
        
    }

    //--- (end of generated code: YDisplayLayer implementation)
};
