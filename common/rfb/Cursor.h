/* Copyright (C) 2002-2005 RealVNC Ltd.  All Rights Reserved.
 * Copyright 2014 Pierre Ossman for Cendio AB
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
//
// Cursor - structure containing information describing
//          the current cursor shape
//

#ifndef __RFB_CURSOR_H__
#define __RFB_CURSOR_H__

#include <rfb/PixelBuffer.h>

namespace rfb {

  class Cursor : public ManagedPixelBuffer {
  public:
    Cursor() {}
    rdr::U8Array mask;
    Point hotspot;

    int maskLen() const { return (width() + 7) / 8 * height(); }

    // setSize() resizes the cursor.  The contents of the data and mask are
    // undefined after this call.
    virtual void setSize(int w, int h);

    // drawOutline() adds an outline to the cursor in the given colour.
    void drawOutline(const Pixel& c);

    // getBitmap() tests whether the cursor is monochrome, and if so returns a
    // bitmap together with background and foreground colours.  The size and
    // layout of the bitmap are the same as the mask.
    rdr::U8* getBitmap(Pixel* pix0, Pixel* pix1) const;

    // crop() crops the cursor down to the smallest possible size, based on the
    // mask.
    void crop();
  };

  class RenderedCursor : public PixelBuffer {
  public:
    RenderedCursor();

    Rect getEffectiveRect() const { return buffer.getRect(offset); }

    virtual const rdr::U8* getBuffer(const Rect& r, int* stride) const;

    void update(PixelBuffer* framebuffer, Cursor* cursor, const Point& pos);

  protected:
    ManagedPixelBuffer buffer;
    Point offset;
  };

}
#endif
