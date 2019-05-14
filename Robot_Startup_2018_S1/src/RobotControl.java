import javax.swing.JOptionPane;

class RobotControl {
	private Robot r;
	public static StringBuilder sb;

	private final int SOURCE_LOCATION = 10; // horizontal position of the column for the source pile of blocks 
	private final int TARGET_1 = 1; //horizontal position of the column for blocks of height 1
	private final int TARGET_2 = 2; // horizontal position of the column for blocks of height 2 
	private final int FIRST_BAR_POSITION = 3; //horizontal position of the column where the first block of size 3 is placed 

	public RobotControl(Robot r) {
		this.r = r;
	}

	public void control(int barHeights[], int blockHeights[])

	{
		run(barHeights, blockHeights); //run the main function (run) 
	}

	public void run(int barHeights[], int blockHeights[]) {
		int h = 2; // Initial height of arm 1
		int w = 1; // Initial width of arm 2
		int d = 0; // Initial depth of arm 3

		int blockHt = 0; // height of the current block
		int currentBar = 0; //instance of the bar within any given loop within the array barHeights
		
		int sourceHt = 0;
		for (int i = 0; i < blockHeights.length; i++) { //calculate the height of the source column of blocks 
			sourceHt = sourceHt + blockHeights[i]; // 

		}

		int targetCol1Ht = 0; // initial height of column 1 
		int targetCol2Ht = 0; // initial height of column 2 

		
		while (h - 1 < sourceHt) {                    // raising the height of the robot to clear the sourceHt at the beginning of  test  
			r.up();  
			h++; // 
		}

		                                               // start of main loop  
		int lastBlock = blockHeights.length - 1;       //lastBlock is used to pull blocks from top of source pile
		for (int i = lastBlock; i >= 0; i--)           // loop until all blocks have been processed 
		{
			blockHt = blockHeights[i]; 					// set height of current block 

			
			System.out.println("Debug 1: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);

			//moving width arm to source pile 
			while (w < SOURCE_LOCATION) { 				//extend the width of the robot to the horizontal position of the source pile 
				r.extend();				
				w++; 					
			}

			System.out.println("Debug 2: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);

			//lowering the depth arm to reach the topmost block on the source pile 
			while (h - d - 1 > sourceHt) {  				
				r.lower();								
				d++; 									
			}
			
			r.pick(); 									// picking the topmost block

			//adjusting robot height to clearance 
			int clearence = getClearence(barHeights, sourceHt, blockHt, currentBar); //call on method to calculate the tallest obstacle the block needs to navigate before the width is contracted 

			while (h - 1 < clearence) {					// raise the height of the robot until it can clear this obstacle 
				r.up();									
				h++; 									
			}

		
			int depthClearence = getdepthClearence(barHeights, sourceHt, blockHt, currentBar, h, d, clearence); // call on the method to calculates the highest obstacle the third arm will needs to raise above to avoid collision

			while (h - 1 - d < depthClearence + blockHt && d > 0 && sourceHt > 0) { //while the height underneath the current block (held by the robot) is less than the highest obstacle the depth arm needs to clear and while  there are still blocks to move
				r.raise();														//raise depth 
				d--; 															
			}

			sourceHt -= blockHt; 										// When you pick the top block, reduce the source height by the height of the block picked  

			System.out.println("Debug 3: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);

			 int contractAmt = getContractAmt (currentBar,blockHt); 	//call on the method to return the value necessary to contract the width arm given the current block and bar 
				while (contractAmt > 0) { 							//while the  amount necessary to contract the width arm is greater than 0 contract the width arm
					r.contract(); 									
					contractAmt--; 								
					w--; 										
				}
			
			System.out.println("Debug 4: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);
			
			int newClearence = getClearence(barHeights, sourceHt, blockHt, currentBar); //return an updated value for clearance now that you have moved the block away from the source pile
			while (h - 1 - d > newClearence) { 										
				r.down(); 															//lower the height component of the robot to take into account the new clearance 
				h--; 																
			}

			int lowerTo = getlowerTo(blockHt, currentBar, barHeights, targetCol1Ht, targetCol2Ht); //return the height on the current target you intend to place the block upon 
			while ((h - 1) - d - blockHt > lowerTo) { 							// lower the depth of the robot arm until block is sitting on the surface 
				r.lower(); 							
				d++; 							
			}

			r.drop(); 								// dropping the block after it is lowered to surface
			
		//adjusting the target column height by the size of the block just placed on it 	
			if (blockHt == 1) { 						
				targetCol1Ht++; 						
			}
			else if (blockHt == 2) { 				
				targetCol2Ht += 2; 					 
			}
			else if (blockHt == 3) {  				
				barHeights[currentBar] += blockHt; 	//increase the height of the current bar to be the current bar plus he blockHt 
				currentBar++; 						//change the current bar to the next current bar in the array 
			}
			
			//raising depth once a block is dropped off 
			int newDepthClearence = getdepthClearence(barHeights, sourceHt, blockHt, currentBar, h, d, clearence); //get a new value for the highest obstacle depth arm has to navigate now that a block has been dropped off 
			
			if (sourceHt > newDepthClearence) {  												//if the sourceHt is the highest obstacle depth arm has to navigate
				while (h - 1 - d < sourceHt && d > 0 && sourceHt > 0) { 							//while the height of the robot is less than sourceHt and while there are still blocks to move raise the depth arm 
					r.raise(); 																					
					d--; 																						
				}
			} 
			else {  																//if the sourceHt is not the highest obstacle depth arm has to navigate
				while (h - 1 - d < newDepthClearence && d > 0 && sourceHt > 0) { // while the height of the robot is less than the highest obstacle depth arm has to navigate raise the depth arm 
					r.raise(); 													
					d--; 														
				}
			}

		}  																		//end of the main loop 

		System.out.println("Debug 5: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);
		System.out.println("Debug 6: height(arm1)= " + h + " width (arm2) = " + w + " depth (arm3) =" + d);

	}

	public int getMaxHeight(int barHeights[]) { 		//get the value for the highest block and bar combination for any given loop 
		int maxHeight = 0; 									

		for (int i = 0; i < barHeights.length; i++){ 			 
			if (barHeights[i] > maxHeight) { 					
				maxHeight = barHeights[i];					
			}
		}
		return maxHeight; 							//returns maxHeight as a value that is the greatest bar and block combination for the current loop 
	}

	public int getClearence(int barHeights[], int sourceHt, int blockHt, int currentBar) { 		//get the value for the clearance, the highest obstacle the robot needs to move past 
		int maxHeight = getMaxHeight(barHeights); 													//call on the method to return the current highest bar and block combination for a given loop 

		int clearence = 0; 																						

		if (maxHeight > barHeights[currentBar] && maxHeight > sourceHt && blockHt == 3) { 							// if the highest bar and block combination is GREATER than the height of the currentBar and the sourceHt
																												
			clearence = maxHeight;																				//clearance is the highest bar and block combination 
		}
		else if (maxHeight > barHeights[currentBar] && maxHeight > sourceHt && blockHt == 1 || blockHt == 2) { 		// if the highest bar and block combination is GREATER than the height of the currentBar and the sourceHt 
																												
			clearence = maxHeight + blockHt;																		//clearance is the highest bar and block combination and the height of the block (needs to raise block past the maxHeight) 
		}
		 else if (maxHeight < barHeights[currentBar] + blockHt && maxHeight > sourceHt && blockHt == 3) { 		// if the currentBar and current block combination will become the maxHeight once the current block is placed on the current bar ent block 
			 																								//and the maxHeight is currently greater than the sourceHt 
			 																								
			 clearence = barHeights[currentBar] + blockHt; 													//clearance is the combination of the current bar height and blockHt  
		} 
		 else if (maxHeight > sourceHt) { 																	// in any other situation where the highest bar and block combination is greater than the sourceHt 
			clearence = maxHeight + blockHt;																	//the clearance is the highest bar and block
		} 
		 else if (maxHeight == sourceHt) { 																	//if the highest bar and block combination is the same as the source pile 
			clearence = maxHeight + blockHt;																	//the clearance is the highest bar and block combination plus the value of the block that needs to move over that height
		}
		 else if (maxHeight < sourceHt && (maxHeight + blockHt) > sourceHt) { 									//if highest bar and block combination is less than the sourceHt 
			 																								//and if the highest bar and block combination plus the current block is higher than the sourceHt 
			clearence = (maxHeight + blockHt);																//clearance is the maxHeight and current block height combination 
		} 
		 else {  																							//in any other situation when the highest bar and block combination is less than the source pile height 
			clearence = sourceHt; 																			// clearance is the source height 
		}

		return clearence; 																					//return the highest  obstacle 
	}

	public int getdepthClearence(int barHeights[], int sourceHt, int blockHt, int currentBar, int h, int d, int clearence) { //calculate the value of the highest obstacle the depth arm needs to navigate 

		int depthObstacle = 0; 																					
		int depthClearence = 0; 																					
		int clearence2 = getClearence(barHeights, sourceHt, blockHt, currentBar); 			//call  method to calculate the tallest obstacle the block needs to navigate before the width is contracted 
		for (int i = currentBar; i < barHeights.length; i++) 								//loop through the array bar heights starting with the current bar (instead of the first instance within the array) 

		{
			if (barHeights[i] > depthObstacle) { 											 
				depthObstacle = barHeights[i];   											//set the depth Obstacle equal to highest bar height value (including and to the right hand side of the current bar)
		}
		}
		int maxHeight = getMaxHeight(barHeights); 											//return the value of the highest bar block combination within the entire array barHeights 

		if (blockHt == 3 && depthObstacle < sourceHt && depthObstacle == maxHeight) { 		
																						// if the highest obstacle including and to the left of the current bar is less than the source height 
																						// and the highest obstacle including and to the left of the current bar is the highest obstacle in the whole array 
			depthClearence = sourceHt; 													//make the depth clearance equal to the source height 
		} 
		else if (blockHt == 3 && depthObstacle < sourceHt && depthObstacle < maxHeight) {
																				 		// if the highest obstacle including and to the left of the current bar is less than the source height 
																						// and the highest obstacle including and to the left of the current bar is less than an obstacle to the left of the current bar (depth could not need to pass over)
			depthClearence = depthObstacle; 												// make the depth clearance equal to the highest obstacle including and to the left of the current bar
		} 
		else if (blockHt == 3 && depthObstacle >= sourceHt) { 								
																						// if the highest obstacle including and to the left of the current bar is greater than or the same as the source height 			
			depthClearence = depthObstacle;												// make the depth clearance equal to the highest obstacle including and to the left of the current bar
		} 
		else if (blockHt == 2 || blockHt == 1 && maxHeight >= sourceHt) { 				
	 																					// if the highest obstacle is less than or equal to the source height 
			depthClearence = maxHeight;													// make the depth clearance equal to the maxHeight
		}
		else if (blockHt == 2 || blockHt == 1 && depthObstacle < sourceHt && depthObstacle < maxHeight) {   
																										// if  the highest obstacle including and to the left of the current bar is less than the source height 
																										// and the highest obstacle including and to the left of the current bar is less than an obstacle to the left of the current bar o
			depthClearence = maxHeight; 																	// / make the depth clearance equal to the maxHeight
		}
		else if (blockHt == 2 || blockHt == 1 && depthObstacle < sourceHt) {								
																										//  in any other case the highest obstacle including and to the left of the current bar is less than the source height 
			depthClearence = sourceHt; 																	// make the depth clearance equal to the sourceHt
		} 
		else {   																						
			depthClearence = clearence2; 																	//make the depth clearance equal to the clearance 
		}
		return depthClearence; 																			// return the value of the obstacle depth will have to navigate
	}
	
	
	public int getContractAmt (int currentBar, int blockHt) { 		//return the amount necessary to contract the width component within a given loop to position any given block above its corresponding column or bar 
		int barPosition = currentBar + FIRST_BAR_POSITION; 											//variable bar position represents the horizontal position of any given bar
																									//(the bars position within the array plus the value of the position of the first bar in the program (a constant))
		
		int contractAmt = 0;																			
		if (blockHt == 3) { 																			
			contractAmt = SOURCE_LOCATION - barPosition; 											//the amount to contract the arm is the difference between the location of the source pile and the position of the current bar within a given loop 
			}
			else if (blockHt == 2)	{ 																
			contractAmt = SOURCE_LOCATION - TARGET_2; 												//the amount to contract the arm is the difference between the location of the source pile and the target column 2 
			}
			else if (blockHt == 1)	{ 																
			 contractAmt = SOURCE_LOCATION - TARGET_1; 												//the amount to contract the arm is the difference between the location of the source pile and the target column 1 
			}
			return contractAmt; 																		//return the value to contract the width arm by 
	
}
	public int getlowerTo (int blockHt, int currentBar, int barHeights[], int targetCol1Ht, int targetCol2Ht) { 	//get the value of the height for the bar or column the current block needs to be placed on 
		int lowerTo = 0; 																	
		if (blockHt == 1) { 																		
			lowerTo =  targetCol1Ht; 															//the height of the current target is equal to the height of target column 1 
	}
		if (blockHt == 2) { 																	
			lowerTo = targetCol2Ht;																// the height of the current target is equal to the height of target column 2
}
		if (blockHt == 3) { 																		
			lowerTo =  barHeights[currentBar]; 													// the height of the current target is equal to the height of the current bar in the array for any given loop 
		}
		return lowerTo; 																			//return the height of the current target you intend to place a block upon 
		}
}