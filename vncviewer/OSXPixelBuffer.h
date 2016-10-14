/* Copyright 2011-2014 Pierre Ossman for Cendio AB
 * 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

#ifndef __OSXPIXELBUFFER_H__
#define __OSXPIXELBUFFER_H__

#include "PlatformPixelBuffer.h"

class OSXPixelBuffer: public PlatformPixelBuffer {
public:
  OSXPixelBuffer(int width, int height);
  ~OSXPixelBuffer();

  virtual void draw(int src_x, int src_y, int x, int y, int w, int h);

protected:
  // This is really a CGContextRef, but Apple headers conflict with FLTK
  void *bitmap;
};


#endif
