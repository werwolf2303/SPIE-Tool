// See https://aka.ms/new-console-template for more information

using System.Buffers.Binary;
using System.Reflection;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.PixelFormats;


class SZSToPng
{
    static byte[] DecompressYaz0(Stream stream)
    {
        using BinaryReader reader = new(stream);

        reader.ReadInt32(); // Magic

        uint decodedLength = BinaryPrimitives.ReverseEndianness(reader.ReadUInt32());

        reader.ReadInt64(); // Padding

        byte[] input = new byte[stream.Length - stream.Position];
        stream.Read(input, 0, input.Length);

        uint inputOffset = 0;

        byte[] output = new byte[decodedLength];
        uint outputOffset = 0;

        ushort mask = 0;
        byte header = 0;

        while (outputOffset < decodedLength)
        {
            if ((mask >>= 1) == 0)
            {
                header = input[inputOffset++];
                mask = 0x80;
            }

            if ((header & mask) != 0)
            {
                if (outputOffset == output.Length)
                {
                    break;
                }

                output[outputOffset++] = input[inputOffset++];
            }
            else
            {
                byte byte1 = input[inputOffset++];
                byte byte2 = input[inputOffset++];

                uint dist = (uint)((byte1 & 0xF) << 8) | byte2;
                uint position = outputOffset - (dist + 1);

                uint length = (uint)byte1 >> 4;
                if (length == 0)
                {
                    length = (uint)input[inputOffset++] + 0x12;
                }
                else
                {
                    length += 2;
                }

                uint gap = outputOffset - position;
                uint nonOverlappingLength = length;

                if (nonOverlappingLength > gap)
                {
                    nonOverlappingLength = gap;
                }

                Buffer.BlockCopy(output, (int)position, output, (int)outputOffset, (int)nonOverlappingLength);
                outputOffset += nonOverlappingLength;
                position += nonOverlappingLength;
                length -= nonOverlappingLength;

                while (length-- > 0)
                {
                    output[outputOffset++] = output[position++];
                }
            }
        }

        return output;
    }

    static void Main(string[] args)
    {
        FileInfo fi = new FileInfo(args[0]);
        FileInfo outDir = new FileInfo(args[1]);
        FileStream fs = fi.Open(FileMode.OpenOrCreate, FileAccess.Read, FileShare.Read);
        StreamReader sr = new StreamReader(fs);
        Stream stream = sr.BaseStream;
        Stream pngStream = new MemoryStream();

        Image image = Image.LoadPixelData<Rgba32>(DecompressYaz0(stream), 256, 256);

        image.SaveAsPng(pngStream);

        var fileStream = File.Create(outDir + "/" + fi.Name.Replace(".szs", ".png"));
        pngStream.Seek(0, SeekOrigin.Begin);
        pngStream.CopyTo(fileStream);
        fileStream.Close();

        sr.Close();
        fs.Close();
    }
}