
ALTER TABLE occperiodeventrule ADD CONSTRAINT occperiodeventrule_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid);

ALTER TABLE cecaserule RENAME TO cecaseeventrule;

ALTER TABLE occperiodeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE cecaseeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE; 

ALTER TABLE public.occperiodeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;

ALTER TABLE public.cecaseeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;

ALTER TABLE muniprofileeventruleset ADD COLUMN cedefault BOOLEAN DEFAULT TRUE;

ALTER TABLE choiceproposal DROP COLUMN hidden;

ALTER TABLE public.eventrule DROP COLUMN promptingproposal_proposalid;

ALTER TABLE public.eventrule ADD COLUMN promptingdirective_directiveid INTEGER CONSTRAINT eventrule_directive_id_FK REFERENCES choicedirective (directiveid);

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (23, 'database/patches/dbpatch_beta23.sql', '08-26-2019', 'ecd', 'occ beta tweaking');