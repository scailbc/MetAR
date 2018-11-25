package it.drakefk.metar;

import java.util.LinkedList;

public class MatrixUtils {

    /**
     * Given a matrix 4x4 return the angles as a rotation angle (in radiants)
     *  around a single axis (x,y,z)
     *  <p>rotX = x*angle; rotY = y*angle; rotZ = z*angle</p>
     *  <p>float convertDegrees = 180f/Math.PI;</p>
     *  <p>rotXDegrees = rotX*convertDegrees</p>
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToAngle/
     * @return new float[]{ angle, x, y, z}: rotX = x*angle; rotY = y*angle; rotZ = z*angle
     */
    public static float[] toAxisAngle(float[] m) {

        float angle,x,y,z; // variables for result
        float epsilon = 0.01f; // margin to allow for rounding errors
        float epsilon2 = 0.1f; // margin to distinguish between 0 and 180 degrees

        // optional check that input is pure rotation, 'isRotationMatrix' is defined at:
        // http://www.euclideanspace.com/maths/algebra/matrix/orthogonal/rotation/
        //assert isRotationMatrix(m) : "not valid rotation matrix" ;// for debugging
        if ((Math.abs(m[4]-m[1])< epsilon)
                && (Math.abs(m[8]-m[2])< epsilon)
                && (Math.abs(m[9]-m[6])< epsilon)) {
            // singularity found

            // first check for identity matrix which must have +1 for all terms
            //  in leading diagonaland zero in other terms
            if ((Math.abs(m[4]+m[1]) < epsilon2)
                    && (Math.abs(m[8]+m[2]) < epsilon2)
                    && (Math.abs(m[9]+m[6]) < epsilon2)
                    && (Math.abs(m[0]+m[5]+m[10]-3) < epsilon2)) {
                // this singularity is identity matrix so angle = 0
                return new float[]{0,1,0,0}; // zero angle, arbitrary axis
            }

            // otherwise this singularity is angle = 180
            angle = (float) Math.PI;

            float xx = (m[0]+1)*1f/2;
            float yy = (m[5]+1)*1f/2;
            float zz = (m[10]+1)*1f/2;
            float xy = (m[4]+m[1])*1f/4;
            float xz = (m[8]+m[2])*1f/4;
            float yz = (m[9]+m[6])*1f/4;

            if ((xx > yy) && (xx > zz)) { // m[0][0] is the largest diagonal term
                if (xx< epsilon) {
                    x = 0;
                    y = 0.7071f;
                    z = 0.7071f;
                } else {
                    x = (float) Math.sqrt(xx);
                    y = xy/x;
                    z = xz/x;
                }
            } else if (yy > zz) { // m[1][1] is the largest diagonal term
                if (yy< epsilon) {
                    x = 0.7071f;
                    y = 0;
                    z = 0.7071f;
                } else {
                    y = (float) Math.sqrt(yy);
                    x = xy/y;
                    z = yz/y;
                }
            } else { // m[2][2] is the largest diagonal term so base result on this
                if (zz< epsilon) {
                    x = 0.7071f;
                    y = 0.7071f;
                    z = 0;
                } else {
                    z = (float) Math.sqrt(zz);
                    x = xz/z;
                    y = yz/z;
                }
            }

            return new float[]{angle,x,y,z}; // return 180 deg rotation
        }

        // as we have reached here there are no singularities so we can handle normally
        float s = (float) Math.sqrt(((m[6] - m[9])*(m[6] - m[9]))
                +((m[8] - m[2])*(m[8] - m[2]))
                +((m[1] - m[4])*(m[1] - m[4]))); // used to normalise
        if (Math.abs(s) < 0.001) s=1;

        // prevent divide by zero, should not happen if matrix is orthogonal and should be
        // caught by singularity test above, but I've left it in just in case
        angle = (float) Math.acos(( m[0] + m[5] + m[10] - 1)*1f/2);
        x = (m[6] - m[9])*1f/s;
        y = (m[8] - m[2])*1f/s;
        z = (m[1] - m[4])*1f/s;
        return new float[]{angle,x,y,z};
    }

    /**
     * Given a matrix 4x4 return the angles around X, Y and Z axis
     * in radians
     *  <p>float convertDegrees = 180f/Math.PI;</p>
     *  <p>rotXDegrees = rotX*convertDegrees</p>
     * http://inside.mines.edu/fs_home/gmurray/ArbitraryAxisRotation/
     * @return new float[]{ rotX, rotY, rotZ}
     */
    public static float[] toXYZAngle(float[] m) {

        float alpha, beta, gamma;

        float sinBeta = -m[2];// m[2][0] = -sin(beta)

        beta = (float) Math.asin(sinBeta);

        /*  m[6] = m[3][1] = cos(beta)*sin(alpha)
         *  m[10] = m[3][2] = cos(beta)*cos(alpha)
         */
        alpha = (float) Math.atan2(m[6], m[10]);

        /*  m[1] = m[1][0] = cos(beta)*sin(gamma)
         *  m[0] = m[0][0] = cos(beta)*cos(gamma)
         */
        gamma = (float) Math.atan2(m[1], m[0]);

        return new float[]{alpha, beta, gamma};

    }



    /**
     * Given a matrix 4x4 return the angles around X, Y and Z axis
     * in radians
     *  <p>float convertDegrees = (float)180f/Math.PI;</p>
     *  <p>rotXDegrees = rotX*convertDegrees</p>
     * http://inside.mines.edu/fs_home/gmurray/ArbitraryAxisRotation/
     * @return new float[]{ rotX, rotY, rotZ}
     */
    public static float[] toZYXAngle(float[] m) {

        float alpha, beta, gamma;

        float sinBeta = m[8];// m[0][2] = sin(beta)

        beta = (float) Math.asin(sinBeta);

        /*  m[9] = m[1][2] = -cos(beta)*sin(alpha)
         *  m[10] = m[2][2] = cos(beta)*cos(alpha)
         */
        alpha = (float) Math.atan2(-m[9], m[10]);


        /*  m[4] = m[0][1] = -cos(beta)*sin(gamma)
         *  m[0] = m[0][0] = cos(beta)*cos(gamma)
         */
        gamma = (float) Math.atan2(-m[4], m[0]);

        return new float[]{alpha, beta, gamma};
    }

    public static float simplifyAngleRad(float angle) {
        return (float)Math.asin(Math.sin(angle));
    }

}
