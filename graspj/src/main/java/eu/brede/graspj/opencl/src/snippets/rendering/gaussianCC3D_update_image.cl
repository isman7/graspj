				float old_intensity = image_buffer[image_index];
				float new_intensity = old_intensity + mu;
				
				uchar old_hue = hue[image_index];
				image_buffer[image_index] = new_intensity;
				
				float z = spots[spot_index+2];
				/*${z_definition}*/
				hue[image_index] = (uchar) rint((old_hue*old_intensity+f_hue(z)*mu)/new_intensity);