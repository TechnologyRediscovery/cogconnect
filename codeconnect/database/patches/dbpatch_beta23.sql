
ALTER TABLE occperiodeventrule ADD CONSTRAINT occperiodeventrule_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid);

ALTER TABLE cecaserule RENAME TO cecaseeventrule;

ALTER TABLE occperiodeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE cecaseeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE; 

ALTER TABLE public.occperiodeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;

ALTER TABLE public.cecaseeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;




INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (23, 'database/patches/dbpatch_beta23.sql', '08-26-2019', 'ecd', 'occ beta tweaking');