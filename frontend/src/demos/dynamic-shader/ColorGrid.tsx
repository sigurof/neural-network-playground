import React, { useEffect, useState } from "react";

// Define a type for individual RGB colors
export type RGBColor = [number, number, number]; // Tuple representing [R, G, B]

// Define the interface for the component props
interface ColorGridProps {
    colors: RGBColor[][];
    width: number;
    height: number;
    imgWidth: number;
}

const ColorGrid: React.FC<ColorGridProps> = ({ colors, width, height, imgWidth }) => {
    const [imageUrl, setImageUrl] = useState<string>('');

    useEffect(() => {
        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');

        if (ctx) {
            colors.forEach((row, y) => {
                row.forEach((color, x) => {
                    ctx.fillStyle = `rgb(${color.join(',')})`;
                    ctx.fillRect(x, y, 1, 1); // Assuming each color block is 1x1 pixels
                });
            });

            setImageUrl(canvas.toDataURL('image/png'));
        }
    }, [colors, width, height]);

    return <img src={imageUrl} alt="Color Grid" style={{ width: imgWidth, height: 'auto' }} />;
};

export default ColorGrid;
