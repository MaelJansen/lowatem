package lowatem;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Joueur implémentant les actions possibles à partir d'un plateau, pour un
 * niveau donné.
 */
public class JoueurLowatem implements IJoueurLowatem {

    /**
     * Cette méthode renvoie, pour un plateau donné et un joueur donné, toutes
     * les actions possibles pour ce joueur.
     *
     * @param plateau le plateau considéré
     * @param couleurJoueur couleur du joueur
     * @param niveau le niveau de la partie à jouer
     * @return l'ensemble des actions possibles
     */
    @Override
    public String[] actionsPossibles(Case[][] plateau, char couleurJoueur, int niveau) {
        // afficher l'heure de lancement
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        System.out.println("actionsPossibles : lancement le " + format.format(new Date()));
        // se préparer à stocker les actions possibles
        ActionsPossibles actions = new ActionsPossibles();
        // calculer les points de vie sur le plateau initial
        NbPointsDeVie nbPv = nbPointsDeVie(plateau);
        deplacementDepuisToutesLesCases(plateau, actions, nbPv, couleurJoueur);
        System.out.println("actionsPossibles : fin");
        return actions.nettoyer();
    }
    
    /**
     * Une fonction qui permet d'ajouter des deplacements depuis n'importe quelle case
     * (grace a un parcours de tableau)
     * testActionpossibles_niveau4
     * @param plateau Le plateau sur lequel on joue
     * @param actions l'ensemble des actions possibles
     * @param nbPv nombre de points de vie total sur le plateau
     */
    void deplacementDepuisToutesLesCases(Case[][] plateau, ActionsPossibles actions, NbPointsDeVie nbPv, char couleurJoueur){
        for (int i = 0; i < Coordonnees.NB_LIGNES; i++) {
            for (int j = 0; j < Coordonnees.NB_COLONNES; j++) {
                // si une unite est presente sur la case et est de la bonne couleur on ajoute un deplacement
                if (plateau[i][j].unitePresente() && estDeLaBonneCouleur(couleurJoueur, plateau, i, j)) {
                    Coordonnees coordonnees = new Coordonnees(i, j);
                    ajoutDeplDepuis((coordonnees), actions, nbPv, plateau);
                }
            }
        }
    }
    
    /**
     * Une qui permet de savoir si le joueur est de la bonne couleur
     * 
     * @param couleur La couleur du joueur qui joue
     * @param plateau Le plateau du je(pour ce niveau)
     * @param i la ligne du plateau qui correspond a l'emplacement du joueur
     * @param j la colonne du plateau qui correspond a l'emplacement du joueur
     * @return Vrai si la couleur du joueur correspond au soldat
     */
    boolean estDeLaBonneCouleur(char couleur, Case[][] plateau, int i, int j){
        boolean bonneCouleur = false;
        // On regarde si la couleur de la case est de la meme couleur que le joueur
        if (plateau[i][j].couleurUnite == couleur){
            bonneCouleur = true;
        }
        return bonneCouleur;
    }
    
    /**
     * Nombre de points de vie de chaque joueur sur le plateau.
     *
     * @param plateau le plateau
     * @return le nombre de pions de cette couleur sur le plateau
     */
    static NbPointsDeVie nbPointsDeVie(Case[][] plateau) {
        int pvRouge = 0;
        int pvNoir = 0;
        for (int i = 0; i < Coordonnees.NB_LIGNES; i++) {
            for (int j = 0; j < Coordonnees.NB_COLONNES; j++) {
                if (plateau[i][j].unitePresente()) {
                    // si c'est un unite noir on ajoute ces points de vie aux noirs
                    if (plateau[i][j].couleurUnite == 'N'){
                        pvNoir = pvNoir + plateau[i][j].pointsDeVie;
                    }else{
                        // si elle est rouge on les ajoutes aux rouges
                        pvRouge = pvRouge + plateau[i][j].pointsDeVie; 
                    }
                }
            }
        }
        return new NbPointsDeVie(pvRouge, pvNoir);
    }

    /**
     * Ajouter tous les déplacements depuis une case donnée.
     *
     * @param coord coordonnées de la case d'origine
     * @param actions ensemble des actions possibles, à compléter
     * @param nbPv nombre de points de vie de chaque joueur sur le plateau
     * initial
     */
    void ajoutDeplDepuis(Coordonnees coord, ActionsPossibles actions, NbPointsDeVie nbPv, Case[][] plateau) {
        // on part dans chacune des 4 directions
        for (Direction dir : Direction.toutes()) {
            ajoutDeplDansDirection(dir, coord, actions, nbPv, plateau);
        }
        // on ajoute le déplacement "sur place"
        ajoutDepl(coord, coord, actions, nbPv);
    }

    /**
     * Ajouter tous les déplacements depuis une case donnée, dans une direction
     * donnée.
     *
     * @param dir direction à suivre
     * @param src coordonnées de la case d'origine
     * @param actions ensemble des actions possibles, à compléter
     * @param nbPv nombre de points de vie de chaque joueur sur le plateau
     * initial
     */
    void ajoutDeplDansDirection(Direction dir, Coordonnees src,
            ActionsPossibles actions, NbPointsDeVie nbPv, Case[][] plateau) {
        Coordonnees dst = src.suivantes(dir);
        while (dst.estDansPlateau()) {
            // Si la case suivante est vide on peut se deplacer
            if (!plateau[dst.ligne][dst.colonne].unitePresente()){
                ajoutDepl(src, dst, actions, nbPv); 
            }
            dst = dst.suivantes(dir);
        }
    }

    /**
     * Ajout d'une action de déplacement dans l'ensemble des actions possibles.
     *
     * @param src coordonnées de la case à l'origine du déplacement
     * @param dst coordonnées de la case destination du déplacement
     * @param actions l'ensemble des actions possibles (en construction)
     * @param nbPv nombre de points de vie de chaque joueur sur le plateau
     * initial
     */
    void ajoutDepl(Coordonnees src, Coordonnees dst, ActionsPossibles actions,
            NbPointsDeVie nbPv) {
          actions.ajouterAction(chaineActionDepl(src, dst, nbPv));   
    }
    
    /**
     * Ajout d'une action d'attaque dans l'ensemble des actions possibles
     * 
     * @param src coordonnées de la case d'origine du deplacement
     * @param dst coordonnées de la case de destination du deplacment
     * @param actions l'ensemble des actions possibles
     * @param nbPv le nombre de points de vie ed chaque joueur sur le plateau
     * @param cible coordonnées de la case de la cible de l'attaque
     */
    void ajoutAttaque(Coordonnees src, Coordonnees dst, ActionsPossibles actions, NbPointsDeVie nbPv, Coordonnees cible){
        actions.ajouterAction(chaineActionAttaque(src, dst, nbPv, cible));
    }
    
    /**
     * Chaîne de caractères correspondant à une action-mesure de déplacement.
     *
     * @param src coordonnées de la case à l'origine du déplacement
     * @param dst coordonnées de la case destination du déplacement
     * @param nbPv nombre de points de vie de chaque joueur après l'action
     * @return la chaîne codant cette action-mesure
     */
    static String chaineActionDepl(Coordonnees src, Coordonnees dst, NbPointsDeVie nbPv) {
        return "" + src.carLigne() + src.carColonne()
                + "D" + dst.carLigne() + dst.carColonne()
                + "," + nbPv.nbPvRouge + "," + nbPv.nbPvNoir;
    }

    /**
     * Chaîne de caractères correspondant à une action d'attaque sur une unité cible
     * 
     * @param src coordonnées de la case d'origine du déplacement
     * @param dst coordonnées de la case de destination du déplacement
     * @param nbPv Nombre de point de vie des deux camps
     * @param cible coordonnées de la case de la cible de l'attque
     * @return la chaîne codant cette action
     */
    static String chaineActionAttaque(Coordonnees src, Coordonnees dst, NbPointsDeVie nbPv, Coordonnees cible) {
        return "" + src.carLigne() + src.carColonne()
                + "D" + dst.carLigne() + dst.carColonne()
                + "," + nbPv.nbPvRouge + "," + nbPv.nbPvNoir
                + "A" + cible.carLigne() + cible.carColonne();
    }
}