##
## Octave file to generate the passer pawn bonuses
##

ranks = 0:7;

factor = 0.8;

possibleSquares = [0, 1, 1, 1, 1, 1, 1, 0];
possibleSquaresO = possibleSquares * factor;
possibleSquaresAux = [possibleSquaresO ; possibleSquares];
possibleSquaresOE = possibleSquaresAux(:);

bonuses=[0, 0, 0, 0.1, 0.3, 0.6, 1, 0];
bonusesO = bonuses * factor;
bonusesAux = [bonusesO ; bonuses];
bonusesOE = bonusesAux(:);
bonuses2 = [bonuses; bonuses](:);

passerValues = 25 * possibleSquaresOE + 100 * bonusesOE;
candidateValues = round(passerValues * 0.5);

outsideValues = round(bonusesOE * 30);

connectedValues = round(bonuses2 * 25);
supportedValues = round(bonuses2 * 55);
mobileValues = round(bonuses2 * 20);
runnerValues = round(bonuses2 * 60);

passerValues = round(passerValues);

otherKingDistanceBonus = round(bonuses * 20);
myKingDistanceBonus = round(bonuses * 10);

#
# Pawn Shield and Storm
#

shieldBonuses=[0, 1, 0.75, 0.5, 0.25, 0, 0, 0];
pawnShieldCenter = round(shieldBonuses * 55);
pawnShield = round(shieldBonuses * 35);

# Storm must be lower than shield
stormBonuses=[0, 0, 0, 0.25, 0.5, 1, 0, 0];
pawnStormCenter = round(stormBonuses * 30);
pawnStorm = round(stormBonuses * 20);

# Print values for the Java evaluator
printf(strrep(strcat(
    "private static final int[] PAWN_CANDIDATE = {",
    substr(sprintf('oe(%i, %i), ', candidateValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER = {",
    substr(sprintf('oe(%i, %i), ', passerValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_OUTSIDE = {",
    substr(sprintf('oe(%i, %i), ', outsideValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_CONNECTED = {",
    substr(sprintf('oe(%i, %i), ', connectedValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_SUPPORTED = {",
    substr(sprintf('oe(%i, %i), ', supportedValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_MOBILE = {",
    substr(sprintf('oe(%i, %i), ', mobileValues), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_RUNNER = {",
    substr(sprintf('oe(%i, %i), ', runnerValues), 1, -2),
    "};\n\n",
    "private static final int[] PAWN_PASSER_OTHER_KING_DISTANCE = {",
    substr(sprintf('oe(0, %i), ', otherKingDistanceBonus), 1, -2),
    "};\n",
    "private static final int[] PAWN_PASSER_MY_KING_DISTANCE = {",
    substr(sprintf('oe(0, %i), ', myKingDistanceBonus), 1, -2),
    "};\n\n",
    "private static final int[] PAWN_SHIELD_CENTER = {",
    substr(sprintf('oe(%i, 0), ', pawnShieldCenter), 1, -2),
    "};\n",
    "private static final int[] PAWN_SHIELD = {",
    substr(sprintf('oe(%i, 0), ', pawnShield), 1, -2),
    "};\n",
    "private static final int[] PAWN_STORM_CENTER = {",
    substr(sprintf('oe(%i, 0), ', pawnStormCenter), 1, -2),
    "};\n",
    "private static final int[] PAWN_STORM = {",
    substr(sprintf('oe(%i, 0), ', pawnStorm), 1, -2),
    "};\n",
    ""
    ), "oe(0, 0)", "0"));